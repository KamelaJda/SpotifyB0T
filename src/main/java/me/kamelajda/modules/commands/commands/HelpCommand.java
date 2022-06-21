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
import me.kamelajda.utils.commands.CommandManager;
import me.kamelajda.utils.commands.ICommand;
import me.kamelajda.utils.commands.SlashContext;
import me.kamelajda.utils.enums.CommandCategory;
import me.kamelajda.utils.language.Language;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

public class HelpCommand extends ICommand {

    private final CommandManager commandManager;

    public HelpCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
        name = "help";
        category = CommandCategory.BASIC;
        commandData = getData();
    }

    @Override
    public boolean execute(SlashContext context) {
        context.getEvent().deferReply(true).queue();

        ICommand command = commandManager.getCommands().get(context.getEvent().getOption("command", OptionMapping::getAsString));

        if (command == null) {
            context.sendTranslate("help.command.not.found");
            return false;
        }

        context.send(embed(command, (context.getEvent().isFromGuild() ? context.getMember() : null), context.getLanguage()).build());

        return true;
    }

    public static EmbedBuilder embed(ICommand cmd, Language language) {
        return embed(cmd, null, language);
    }

    public static EmbedBuilder embed(ICommand cmd, @Nullable Member author, Language language) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(String.format(language.get("help.command"), "/", cmd.getName()));

        eb.addField(
                language.get("help.command.description"),
                language.get(cmd.getName() + ".description"),
                false);
        eb.addField(
                language.get("help.command.category"),
                language.get("category." + cmd.getCategory().name().toLowerCase()),
                false);

        String key = cmd.getName() + ".extra.help";
        String dodatkowaPomoc = language.get(key);

        if (!dodatkowaPomoc.equals(key)) {
            eb.addField(language.get("help.t.extra.help"), "```\n" + dodatkowaPomoc + "```", false);
        }

        if (author != null) eb.setColor(UserUtil.getColor(author));

        return eb;
    }
}
