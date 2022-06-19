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

import com.google.errorprone.annotations.CheckReturnValue;
import java.util.ArrayList;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.kamelajda.utils.language.Language;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.sharding.ShardManager;

@RequiredArgsConstructor
@Getter
public class SlashContext {

    public static final Pattern URLPATTERN =
            Pattern.compile(
                    "(https?://(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\."
                            + "[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?://(?:www\\.|(?!www))[a-zA-Z0-9]"
                            + "\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]\\.[^\\s]{2,})");

    private final SlashCommandInteractionEvent event;
    private final String prefix;
    private final ICommand cmd;
    private final Language language;

    public Member getMember() {
        return event.getMember();
    }

    public User getUser() {
        return event.getUser();
    }

    public Guild getGuild() {
        return event.getGuild();
    }

    @CheckReturnValue
    public String getTranslate(String msg) {
        return language.get(msg);
    }

    @CheckReturnValue
    public String getTranslate(String key, String... args) {
        return language.get(key, args);
    }

    @CheckReturnValue
    public String getTranslate(String key, Object... args) {
        ArrayList<String> parsedArgi = new ArrayList<>();
        for (Object arg : args) {
            parsedArgi.add(arg.toString());
        }
        return language.get(key, parsedArgi.toArray(new String[] {}));
    }

    public JDA getJDA() {
        return event.getJDA();
    }

    public ShardManager getShardManager() {
        return event.getJDA().getShardManager();
    }

    public InteractionHook getHook() {
        return getEvent().getHook();
    }

    public void sendTranslate(String key, Object... obj) {
        send(getTranslate(key, obj));
    }

    public Message sendTranslate(String key) {
        return send(getTranslate(key));
    }

    public Message send(CharSequence msg) {
        return send(msg, true);
    }

    public Message send(CharSequence msg, boolean checkUrl) {
        String message = String.valueOf(msg);
        if (checkUrl && URLPATTERN.matcher(msg).matches()) {
            message = message.replaceAll(String.valueOf(URLPATTERN), "[LINK]");
        }
        return getEvent()
                .getHook()
                .sendMessage(message.replaceAll("@(everyone|here)", "@\u200b$1"))
                .complete();
    }

    public Message send(MessageEmbed message) {
        return event.getHook().sendMessageEmbeds(message).complete();
    }

    public Message send(Message message) {
        return event.getHook().sendMessage(message).complete();
    }

    public MessageChannel getChannel() {
        return getEvent().getChannel();
    }
}
