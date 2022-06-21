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

package me.kamelajda.modules.logs.listeners;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kamelajda.utils.Listener;
import me.kamelajda.utils.UserUtil;
import me.kamelajda.utils.WebhookUtil;
import me.kamelajda.utils.events.CommandExecuteEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import org.springframework.core.env.Environment;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
@Slf4j
public class CommandExecuteListener implements Listener {

    private final Environment environment;

    @Subscribe
    public void onCommand(CommandExecuteEvent e) {
        Instant now = Instant.now();

        MessageChannel channel = e.getContext().getChannel();
        String place = String.format("%s #%s[%s]",
            e.getContext().getEvent().isFromGuild()
                ? "guild " + e.getContext().getEvent().getGuild().getName() + "[" + e.getContext().getEvent().getGuild().getId() + "]"
                : "pv",
            channel.getName(), channel.getId()
        );

        log.info("User " + UserUtil.getLogName(e.getContext().getUser()) + " executed command: '" + e.getContext().getEvent().getCommandString() + "' on " + place);

        WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
        builder.setTimestamp(now);
        builder.setAuthor(
            new WebhookEmbed.EmbedAuthor(
                e.getContext().getUser().getAsTag(),
                e.getContext().getUser().getEffectiveAvatarUrl(),
                null
            )
        );

        builder.addField(new WebhookEmbed.EmbedField(false, "Command", e.getContext().getEvent().getCommandString()));
        builder.addField(new WebhookEmbed.EmbedField(false, "User", UserUtil.getLogName(e.getContext().getUser())));
        builder.addField(new WebhookEmbed.EmbedField(false, "Place", place));

        builder.addField(new WebhookEmbed.EmbedField(false, "Time info",
            e.getContext().getHook().getInteraction().getTimeCreated()
                .minus(now.toEpochMilli(), ChronoUnit.MILLIS).toInstant().toEpochMilli() + "ms"));

        WebhookUtil.builder()
            .env(environment)
            .type(WebhookUtil.LogType.CMD)
            .embed(builder.build())
            .build()
            .send();

    }

    @Subscribe
    public void onGuildJoin(GuildJoinEvent e) {
        WebhookUtil.builder()
            .env(environment)
            .type(WebhookUtil.LogType.GUILD)
            .embed(guildEmbed("New guild found!", e.getGuild()))
            .build()
            .send();
    }

    @Subscribe
    public void onGuildLeave(GuildLeaveEvent e) {
        WebhookUtil.builder()
            .env(environment)
            .type(WebhookUtil.LogType.GUILD)
            .embed(guildEmbed("I lost guild...", e.getGuild()))
            .build()
            .send();
    }

    private WebhookEmbed guildEmbed(String title, Guild guild) {
        WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
        builder.setTimestamp(Instant.now());

        builder.setTitle(new WebhookEmbed.EmbedTitle(title, null));
        builder.addField(new WebhookEmbed.EmbedField(false, "Guild", String.format("%s[%s]", guild.getName(), guild.getId())));
        builder.addField(new WebhookEmbed.EmbedField(false, "Guild locale", guild.getLocale().getDisplayName()));

        if (guild.getOwner() != null) {
            builder.addField(new WebhookEmbed.EmbedField(false, "Owner", UserUtil.getLogName(guild.getOwner())));
        } else {
            builder.addField(new WebhookEmbed.EmbedField(false, "Owner", guild.getOwnerId()));
        }

        return builder.build();
    }

}
