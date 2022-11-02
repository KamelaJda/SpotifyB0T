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

import lombok.extern.slf4j.Slf4j;
import me.kamelajda.jpa.models.GuildConfig;
import me.kamelajda.services.GuildConfigService;
import me.kamelajda.utils.Static;
import me.kamelajda.utils.UserUtil;
import me.kamelajda.utils.commands.ICommand;
import me.kamelajda.utils.commands.SlashContext;
import me.kamelajda.utils.commands.SubCommand;
import me.kamelajda.utils.enums.CommandCategory;
import me.kamelajda.utils.language.LanguageType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.time.Instant;

@Slf4j
public class ConfigureGuildCommand extends ICommand {

    private static final String FORMAT = "%s **➔** %s";

    private final GuildConfigService guildConfigService;

    public ConfigureGuildCommand(GuildConfigService guildConfigService) {
        this.guildConfigService = guildConfigService;
        name = "configureguild";
        category = CommandCategory.CONFIGURE;
        onlyInGuild = true;
        requiredPermissions = DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER);
    }

    @SubCommand(name = "set")
    public void set(SlashContext context) {
        OptionMapping language = context.getEvent().getOption("language");
        OptionMapping notification = context.getEvent().getOption("notification");

        if (language == null && notification == null) {
            context.getEvent().deferReply(true).queue();
            context.sendTranslate("configureguild.not.args");
            return;
        }

        context.getEvent().deferReply(false).queue();

        GuildConfig config = guildConfigService.load(context.getGuild().getIdLong());

        EmbedBuilder eb = embed(context);

        if (language != null) {
            String s = language.getAsString();
            LanguageType type = LanguageType.valueOf(s);

            eb.addField(context.getLanguage().get("configureguild.changed.language"), String.format(FORMAT, config.getLanguage().getDisplayName(), type.getDisplayName()), false);
            config.setLanguage(type);
        }

        if (notification != null) {
            GuildChannelUnion channel = notification.getAsChannel();

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
    }

    @SubCommand(name = "remove")
    public void remove(SlashContext context) {
        String key = context.getEvent().getOption("key").getAsString();

        context.getEvent().deferReply(false).queue();

        GuildConfig config = guildConfigService.load(context.getGuild().getIdLong());

        EmbedBuilder eb = embed(context);

        switch (key) {
            case "reset-language" -> {
                eb.appendDescription(context.getLanguage().get("configureguild.deleted.language", Static.defualtLanguage));
                config.setLanguage(Static.defualtLanguage.getLanguageType());
                break;
            }
            case "reset-notification" -> {
                eb.appendDescription(context.getLanguage().get("configureguild.deleted.notification.channel"));
                config.setNotificationChannelId(null);
                break;
            }
            default -> eb.appendDescription(context.getLanguage().get("configureguild.deleted.error"));
        }

        context.send(eb.build());

        guildConfigService.save(config);
    }

    @Override
    protected void updateSubcommandData(SubcommandData subcommandData, String key) {
        if (key.equals("set")) setSubcommands(subcommandData);
        else if (key.equals("remove")) removeCommands(subcommandData);
    }

    private EmbedBuilder embed(SlashContext context) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(UserUtil.getColor(context.getMember()));
        eb.setTimestamp(Instant.now());
        eb.setTitle(context.getLanguage().get("configureguild.changed.embed.title"));
        eb.setAuthor(context.getUser().getAsTag(), null, context.getUser().getEffectiveAvatarUrl());

        return eb;
    }

    private static void setSubcommands(SubcommandData setData) {
        OptionData notificationChannel = new OptionData(OptionType.CHANNEL, "notification", "Choose notification channel");
        notificationChannel.setChannelTypes(ChannelType.TEXT);

        setData.addOptions(notificationChannel);
    }

    private static SubcommandData removeCommands(SubcommandData removeData) {
        OptionData key = new OptionData(OptionType.STRING, "key", "Remove key", true, false);

        key.addChoice("clearnotify", "reset-notification");

        return removeData.addOptions(key);
    }

}
