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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.kamelajda.utils.EmbedPaginator;
import me.kamelajda.utils.EventWaiter;
import me.kamelajda.utils.UserUtil;
import me.kamelajda.utils.commands.ICommand;
import me.kamelajda.utils.commands.SlashContext;
import me.kamelajda.utils.enums.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ContributorsCommand extends ICommand {

    private static JsonArray CONTRIBUTORS;

    static {
        try {
            CONTRIBUTORS = new Gson().fromJson(new FileReader("contributors.json"), JsonArray.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private final EventWaiter eventWaiter;

    public ContributorsCommand(EventWaiter eventWaiter) {
        this.eventWaiter = eventWaiter;
        name = "contributors";
        category = CommandCategory.BASIC;
    }

    @Override
    protected void execute(SlashContext context) {
        context.getEvent().deferReply(true).queue();
        context.sendTranslate("global.generic.loading");

        List<EmbedBuilder> pages = new ArrayList<>();

        for (JsonElement element : CONTRIBUTORS) {
            try {
                pages.add(embed(context, element.getAsJsonObject()));
            } catch (Exception ignored) { }
        }

        EmbedPaginator.create(pages, context.getUser(), eventWaiter, context.getHook());
    }

    private static EmbedBuilder embed(SlashContext context, JsonObject object) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setFooter("SpotifyB0T", context.getJDA().getSelfUser().getEffectiveAvatarUrl());
        eb.setTimestamp(Instant.now());
        eb.setColor(UserUtil.getColor(context.getMember(), context.getShardManager()));

        long userId = object.get("user").getAsLong();
        User user = context.getShardManager().retrieveUserById(userId).complete();

        if (user != null) {
            eb.setAuthor(user.getAsTag(), "https://discord.com/users/" + user.getIdLong(), user.getEffectiveAvatarUrl());
            eb.setImage(user.getEffectiveAvatarUrl() + "?size=2048");


            JsonArray array = object.get("types").getAsJsonArray();

            for (int i = 0; i < array.size(); i++) {
                ContributorType type = ContributorType.valueOf(array.get(i).getAsString());

                eb.appendDescription(
                    " • " +
                    context.getLanguage().get("contributors.type." + type.name().toLowerCase())
                );

                if (i + 1 < array.size()) eb.appendDescription("\n");
            }

        } else {
            eb.setAuthor(String.valueOf(userId));
        }

        return eb;
    }

    private enum ContributorType {
        DEVELOPER, TRANSLATOR, BUG_HUNTER
    }

}
