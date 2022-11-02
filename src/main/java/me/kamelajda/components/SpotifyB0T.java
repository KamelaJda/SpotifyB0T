/*
 *
 * Copyright 2022 SpotifyB0T
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package me.kamelajda.components;

import com.google.common.eventbus.EventBus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.kamelajda.modules.commands.CommandModule;
import me.kamelajda.modules.logs.LogsModule;
import me.kamelajda.redis.services.RedisStateService;
import me.kamelajda.services.GuildConfigService;
import me.kamelajda.services.SpotifyService;
import me.kamelajda.services.SubscribeArtistService;
import me.kamelajda.services.UserConfigService;
import me.kamelajda.utils.EventWaiter;
import me.kamelajda.utils.Static;
import me.kamelajda.utils.commands.CommandExecute;
import me.kamelajda.utils.commands.CommandManager;
import me.kamelajda.utils.language.Language;
import me.kamelajda.utils.language.LanguageService;
import me.kamelajda.utils.language.LanguageType;
import me.kamelajda.utils.listener.JDAHandler;
import me.kamelajda.utils.module.IModule;
import me.kamelajda.utils.module.ModuleManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.ResourceBundleLocalizationFunction;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class SpotifyB0T {

    private final String token;
    private final LanguageType defaultLanguage;
    private final EventBus eventBus;
    private final SubscribeArtistService subscribeArtistService;
    private final EventWaiter eventWaiter;
    private final SpotifyService spotifyService;
    private final LanguageService languageService;
    private final GuildConfigService guildConfigService;
    private final UserConfigService userConfigService;
    private final Environment env;
    private final RedisStateService redisStateService;

    @Getter
    private ShardManager api;

    public SpotifyB0T(Environment env, EventBus eventBus, SubscribeArtistService subscribeArtistService, EventWaiter eventWaiter, SpotifyService spotifyService, LanguageService languageService, GuildConfigService guildConfigService, UserConfigService userConfigService, RedisStateService redisStateService) {
        this.env = env;
        this.subscribeArtistService = subscribeArtistService;
        this.eventBus = eventBus;
        this.eventWaiter = eventWaiter;
        this.spotifyService = spotifyService;
        this.languageService = languageService;
        this.token = env.getProperty("discord.bot.token");
        this.defaultLanguage = LanguageType.valueOf(env.getProperty("application.defaultLanguage"));
        this.guildConfigService = guildConfigService;
        this.userConfigService = userConfigService;
        this.redisStateService = redisStateService;

        start();
    }

    public void start() {
        Language language = languageService.get(defaultLanguage);
        Static.defualtLanguage = language;

        log.info(language.get("status.translation.loaded"));

        CommandManager commandManager = new CommandManager();
        ModuleManager moduleManager = new ModuleManager();
        CommandExecute commandExecute = new CommandExecute(commandManager, userConfigService, languageService, guildConfigService, eventBus);
        JDAHandler eventHandler = new JDAHandler(eventBus);

        try {
            DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.GUILD_MEMBERS);
            builder.addEventListeners(eventHandler, eventWaiter);
            builder.setShardsTotal(Objects.requireNonNull(env.getProperty("jda.shards.total", Integer.class)));
            builder.setShards(Objects.requireNonNull(env.getProperty("jda.shards.min", Integer.class)), Objects.requireNonNull(env.getProperty("jda.shards.max", Integer.class)));
            builder.setEnableShutdownHook(false);
            builder.setAutoReconnect(true);
            builder.setStatus(OnlineStatus.DO_NOT_DISTURB);
            builder.setActivity(Activity.playing(language.get("status.starting")));
            builder.setBulkDeleteSplittingEnabled(false);
            builder.setCallbackPool(Executors.newFixedThreadPool(30));
            builder.enableCache(CacheFlag.MEMBER_OVERRIDES);
            builder.disableCache(CacheFlag.VOICE_STATE);
            MessageRequest.setDefaultMentionRepliedUser(false);
            MessageRequest.setDefaultMentions(EnumSet.of(Message.MentionType.EMOJI, Message.MentionType.CHANNEL));
            this.api = builder.build();
        } catch (Exception e) {
            log.error("Failed to login!", e);
            System.exit(1);
        }

        List<IModule> modules = new ArrayList<>();
        modules.add(
            new CommandModule(
                commandManager, subscribeArtistService,
                spotifyService, eventWaiter, eventBus, guildConfigService,
                userConfigService, languageService, redisStateService
            )
        );

        modules.add(new LogsModule(eventBus, env));

        for (IModule module : modules) {
            moduleManager.loadModule(module);
        }

        OptionData optionData = new OptionData(OptionType.STRING, "command", "command", true);
        for (String s : commandManager.getCommands().keySet()) {
            optionData.addChoice(s, s);
        }

        LocalizationFunction localizationFunction = ResourceBundleLocalizationFunction
            .fromBundles("language/messages", Arrays.stream(LanguageType.values()).map(LanguageType::getDiscordLocale).toArray(DiscordLocale[]::new))
            .build();

        List<CommandDataImpl> data = commandManager.getCommands().values().stream()
            .map(cmd -> cmd.createCommandDate(localizationFunction)).toList();

        for (JDA shard : api.getShards()) {
            shard.updateCommands().addCommands(data).queue();
        }

        eventBus.register(commandExecute);

        api.setStatus(OnlineStatus.ONLINE);
        api.setActivity(Activity.playing(language.get("status.hi")));

        spotifyService.setShardManager(api);
    }
}
