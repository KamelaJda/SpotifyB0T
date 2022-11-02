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

import me.kamelajda.jpa.models.UserConfig;
import me.kamelajda.services.UserConfigService;
import me.kamelajda.utils.UserUtil;
import me.kamelajda.utils.commands.ICommand;
import me.kamelajda.utils.commands.SlashContext;
import me.kamelajda.utils.enums.CommandCategory;
import me.kamelajda.utils.language.LanguageType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.Instant;

@Deprecated
public class ConfigureUserCommand extends ICommand {

    private static final String FORMAT = "%s **->** %s";

    private final UserConfigService userConfigService;

    public ConfigureUserCommand(UserConfigService userConfigService) {
        this.userConfigService = userConfigService;
        name = "configureuser";
        category = CommandCategory.CONFIGURE;
        onlyInGuild = true;

        OptionData languageOption = new OptionData(OptionType.STRING, "language", "Choose language of server");

        for (LanguageType value : LanguageType.values()) {
            languageOption.addChoice(value.getDisplayName(), value.name());
        }

//        commandData = getData().addOptions(languageOption);
    }

    @Override
    protected void execute(SlashContext context) {
        OptionMapping language = context.getEvent().getOption("language");
        context.getEvent().deferReply(true).queue();

        if (language == null) {
            context.sendTranslate("configureuser.not.args");
            return;
        }

        UserConfig config = userConfigService.load(context.getUser().getIdLong());

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(UserUtil.getColor(context.getMember()));
        eb.setTimestamp(Instant.now());
        eb.setTitle(context.getLanguage().get("configureuser.changed.embed.title"));
        eb.setAuthor(context.getUser().getAsTag(), null, context.getUser().getEffectiveAvatarUrl());

        String s = language.getAsString();
        LanguageType type = LanguageType.valueOf(s);

//        eb.addField(context.getLanguage().get("configureuser.changed.language"), String.format(FORMAT, config.getLanguageType().getDisplayName(), type.getDisplayName()), false);
//        config.setLanguageType(type);

        context.send(eb.build());

        userConfigService.save(config);
    }


}
