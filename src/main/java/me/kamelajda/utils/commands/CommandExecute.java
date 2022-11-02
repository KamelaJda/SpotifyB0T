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

package me.kamelajda.utils.commands;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kamelajda.jpa.models.UserConfig;
import me.kamelajda.services.GuildConfigService;
import me.kamelajda.services.UserConfigService;
import me.kamelajda.utils.events.CommandExecuteEvent;
import me.kamelajda.utils.language.LanguageService;
import me.kamelajda.utils.language.LanguageType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
public class CommandExecute {

    private final CommandManager commandManager;
    private final ExecutorService executor = Executors.newFixedThreadPool(8);
    private final UserConfigService userConfigService;
    private final LanguageService languageService;
    private final GuildConfigService guildConfigService;
    private final EventBus eventBus;

    @Subscribe
    public void onSlashCommand(SlashCommandInteractionEvent e) {
        String channelId = e.getGuild() == null ? "dm" : e.getGuild().getId();

        Runnable run = () -> {
            Thread.currentThread().setName(e.getUser().getId() + "-" + e.getName() + "-" + channelId);
            ICommand c = commandManager.commands.get(e.getName());
            if (c != null) {
                UserConfig userConfig = userConfigService.load(e.getUser().getIdLong());

                if (userConfig.getLanguageType() == null && userConfig.getLanguageType().getDiscordLocale() != e.getUserLocale()) {
                    userConfig.setLanguageType(LanguageType.fromDiscord(e.getUserLocale()));
                    userConfigService.save(userConfig);
                }

                SlashContext context = new SlashContext(e, "/", c, userConfig,
                    languageService.get(LanguageType.fromDiscord(e.getUserLocale())),
                    e.isFromGuild() ? guildConfigService.load(e.getGuild().getIdLong()) : null);

                if (c.isOnlyInGuild() && !e.isFromGuild()) {
                    e.deferReply(true).queue();
                    e.getHook().sendMessage(context.getLanguage().get("global.command.only.enabled.in.guild")).queue();
                    return;
                }

                try {
                    eventBus.post(new CommandExecuteEvent(context));
                    c.preExecute(context);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    context.getHook().sendMessage(context.getLanguage().get("global.command.error")).complete();
                }

            }
        };

        executor.execute(run);
    }
}