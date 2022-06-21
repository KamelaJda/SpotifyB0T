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

import me.kamelajda.utils.UserUtil;
import me.kamelajda.utils.commands.ICommand;
import me.kamelajda.utils.commands.SlashContext;
import me.kamelajda.utils.enums.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;

public class ReportBugCommand extends ICommand {

    private static final String ISSUE_LINK = "https://github.com/KamelaJda/SpotifyB0T/issues/new/choose";

    public ReportBugCommand() {
        name = "reportbug";
        category = CommandCategory.BASIC;
        commandData = getData();
    }

    @Override
    protected boolean execute(SlashContext context) {
        context.getEvent().deferReply(true).queue();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setFooter("SpotifyB0T", context.getJDA().getSelfUser().getEffectiveAvatarUrl());
        eb.setTimestamp(Instant.now());
        eb.setDescription(context.getLanguage().get("reportbug.embed.description", ISSUE_LINK));
        eb.setColor(UserUtil.getColor(context.getMember(), context.getShardManager()));

        context.send(eb.build());
        return true;
    }

}
