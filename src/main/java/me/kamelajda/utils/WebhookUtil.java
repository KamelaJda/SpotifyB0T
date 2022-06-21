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

package me.kamelajda.utils;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.core.env.Environment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@SuppressWarnings("ConstantConditions")
@Data
@AllArgsConstructor
@Builder
public class WebhookUtil {

    private final SimpleDateFormat SDF = new SimpleDateFormat("MM.dd HH:mm:ss.SSS");

    private LogType type;
    private String name;
    private String message;
    private String avatar;
    private String url;
    private WebhookEmbed embed;
    private Environment env;

    public void send() {
        if (type == null) throw new NullPointerException("type == null");
        WebhookClient client = WebhookClient.withUrl(getEnv().getProperty("webhook.logs." + getType().name().toLowerCase()));
        WebhookMessageBuilder builder = new WebhookMessageBuilder();

        if (getName() == null) builder.setUsername(getType().getDisplayName());
        else builder.setUsername(getName());

        if (avatar != null) builder.setAvatarUrl(getAvatar());
        if (getMessage() != null && !getMessage().isEmpty()) builder.setContent(String.format("[%s] %s", SDF.format(Calendar.getInstance().getTime()), getMessage()));
        if (getEmbed() != null) builder.addEmbeds(getEmbed());
        client.send(builder.build());
        client.close();
    }

    @AllArgsConstructor
    @Getter
    public enum LogType {
        GUILD("Join/Leave logs"),
        CMD("Commands Logs");

        private final String displayName;
    }

}