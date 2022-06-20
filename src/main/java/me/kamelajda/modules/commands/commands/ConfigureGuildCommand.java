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

package me.kamelajda.modules.commands.commands;

import me.kamelajda.jpa.models.GuildConfig;
import me.kamelajda.services.GuildConfigService;
import me.kamelajda.utils.UserUtil;
import me.kamelajda.utils.commands.ICommand;
import me.kamelajda.utils.commands.SlashContext;
import me.kamelajda.utils.enums.CommandCategory;
import me.kamelajda.utils.language.LanguageType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.Instant;
import java.util.Set;

public class ConfigureGuildCommand extends ICommand {

    private static final String FORMAT = "%s **->** %s";

    private final GuildConfigService guildConfigService;

    public ConfigureGuildCommand(GuildConfigService guildConfigService) {
        this.guildConfigService = guildConfigService;
        name = "configureguild";
        category = CommandCategory.CONFIGURE;
        onlyInGuild = true;
        requiredPermissions = Set.of(Permission.MANAGE_SERVER);

        OptionData languageOption = new OptionData(OptionType.STRING, "language", "Choose language of server");

        for (LanguageType value : LanguageType.values()) {
            languageOption.addChoice(value.getDisplayName(), value.name());
        }

        OptionData notificationChannel = new OptionData(OptionType.CHANNEL, "notification", "Choose notification channel");

        commandData = getData().addOptions(languageOption, notificationChannel);
    }

    @Override
    protected boolean execute(SlashContext context) {
        OptionMapping language = context.getEvent().getOption("language");
        OptionMapping notification = context.getEvent().getOption("notification");

        if (language == null && notification == null || language == null && notification.getAsTextChannel() == null) {
            context.getEvent().deferReply(true).queue();
            context.sendTranslate("configureguild.not.args");
            return false;
        }

        context.getEvent().deferReply(false).queue();

        GuildConfig config = guildConfigService.load(context.getGuild().getIdLong());

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(UserUtil.getColor(context.getMember()));
        eb.setTimestamp(Instant.now());
        eb.setTitle(context.getLanguage().get("configureguild.changed.embed.title"));
        eb.setAuthor(context.getUser().getAsTag(), null, context.getUser().getEffectiveAvatarUrl());

        if (language != null) {
            String s = language.getAsString();
            LanguageType type = LanguageType.valueOf(s);

            eb.addField(context.getLanguage().get("configureguild.changed.language"), String.format(FORMAT, config.getLanguage().getDisplayName(), type.getDisplayName()), false);
            config.setLanguage(type);
        }

        if (notification != null && notification.getAsTextChannel() != null) {
            TextChannel channel = notification.getAsTextChannel();

            String channelName = null;
            if (config.getNotificationChannelId() != null) {
                TextChannel id = context.getGuild().getTextChannelById(config.getNotificationChannelId());
                if (id != null) channelName = id.getAsMention();
            } else channelName = context.getLanguage().get("configureguild.value.not.set");

            eb.addField(context.getLanguage().get("configureguild.changed.notification.channel"), String.format(FORMAT, channelName, channel.getAsMention()), false);

            config.setNotificationChannelId(channel.getIdLong());
        }

        context.send(eb.build());

        guildConfigService.save(config);

        return true;
    }

}
