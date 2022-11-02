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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.kamelajda.utils.Static;
import me.kamelajda.utils.enums.CommandCategory;
import me.kamelajda.utils.language.Language;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.springframework.lang.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Getter
@Slf4j
public abstract class ICommand {

    protected String name;
    protected CommandCategory category;
    protected boolean onlyInGuild = false;
    protected DefaultMemberPermissions requiredPermissions = DefaultMemberPermissions.ENABLED;
    protected String usage = "";

    protected final Map<String, Method> subCommands = new HashMap<>();

    protected final List<SubcommandData> discordSubcommands = new ArrayList<>();

    public void preExecute(SlashContext context) throws InvocationTargetException, IllegalAccessException {
        if (context.getEvent().getSubcommandName() != null && getSubCommands().containsKey(context.getEvent().getSubcommandName())) {
            Method method = getSubCommands().get(context.getEvent().getSubcommandName());

            method.invoke(this, context);
            return;
        }

        execute(context);
    }

    protected void execute(SlashContext context) {
        throw new UnsupportedOperationException("Komenda nie ma zaimplementowanej funkcji execute(SlashContext)");
    }

    @Override
    public String toString() {
        return this.name;
    }

    public CommandDataImpl createCommandDate(LocalizationFunction localizationFunction) {
        CommandDataImpl data = new CommandDataImpl(this.name, Static.defualtLanguage.get(this.name + ".description"));

        try {
            Language language = Static.defualtLanguage;

            data.setDescriptionLocalization(language.getLanguageType().getDiscordLocale(), language.get(this.name + ".description"));
            data.addOptions(generateOptionData(language, getUsage(), null));
            data.setNameLocalization(language.getLanguageType().getDiscordLocale(), language.get(this.name + ".name"));
            data.setLocalizationFunction(localizationFunction);
            data.setDefaultPermissions(requiredPermissions);

            if (subCommands.size() > 0) {
                List<SubcommandData> scd = new ArrayList<>();

                for (Map.Entry<String, Method> entry : subCommands.entrySet()) {
                    SubcommandData subcommandData = new SubcommandData(entry.getKey(), "random opis");

                    SubCommand command = entry.getValue().getAnnotation(SubCommand.class);

                    if (!command.usage().isBlank()) {
                        subcommandData.addOptions(generateOptionData(language, command.usage(), entry.getKey()));
                    }

                    scd.add(subcommandData);

                    updateSubcommandData(subcommandData, entry.getKey());
                }

                data.addSubcommands(scd);
            }

        } catch (Exception e) {
            log.error("CreateCommandData Error!", e);
        }

        return data;
    }

    protected void updateOptionData(OptionData optionData, String key, String subcommand) { }

    protected void updateSubcommandData(SubcommandData subcommandData, String key) { }

    public List<OptionData> generateOptionData(Language language, String usage, @Nullable String subcommand) {
        if (usage.isEmpty()) return new ArrayList<>();

        String[] arguments = usage.split(" ");
        List<OptionData> options = new ArrayList<>();

        for (String arg : arguments) {
            boolean required = arg.startsWith("<") && arg.endsWith(">");
            if (!required && !arg.startsWith("[") || !arg.endsWith("]"))
                throw new IllegalStateException("Invalid argument " + arg);

            String[] argData = arg.substring(1, arg.length() - 1).split(":");
            if (argData.length != 2) throw new IllegalStateException("Invalid argument data " + arg);

            OptionType optionType;
            String key = argData[0];

            switch (argData[1]) {
                case "string" -> optionType = OptionType.STRING;
                case "integer" -> optionType = OptionType.INTEGER;
                case "boolean" -> optionType = OptionType.BOOLEAN;
                case "user" -> optionType = OptionType.USER;
                case "channel", "textchannel", "voicechannel" -> optionType = OptionType.CHANNEL;
                case "role" -> optionType = OptionType.ROLE;
                case "mentionable" -> optionType = OptionType.MENTIONABLE;
                case "double" -> optionType = OptionType.NUMBER;
                case "attachment" -> optionType = OptionType.ATTACHMENT;
                default -> throw new IllegalArgumentException("Invalid argument type: " + argData[1]);
            }

            OptionData op = new OptionData(optionType, key, language.get(this.name + "." + key + ".description"), required, false);

            if (optionType == OptionType.CHANNEL) {
                EnumSet<ChannelType> types = EnumSet.noneOf(ChannelType.class);

                if (key.equals("textchannel")) types.addAll(Set.of(ChannelType.TEXT, ChannelType.NEWS));
                else if (key.equals("voicechannel")) types.add(ChannelType.VOICE);

                op.setChannelTypes(types);
            }

            updateOptionData(op, key, subcommand);
            options.add(op);
        }

        return options;
    }

}
