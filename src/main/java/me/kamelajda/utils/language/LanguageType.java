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

package me.kamelajda.utils.language;

import com.neovisionaries.i18n.CountryCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.kamelajda.utils.Static;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.time.ZoneId;

@Getter
@AllArgsConstructor
public enum LanguageType {
    ENGLISH("en_US", "English (US)", ZoneId.of("US/Central"), CountryCode.US, DiscordLocale.ENGLISH_US),
    POLISH("pl", "Polski", ZoneId.of("Europe/Warsaw"), CountryCode.PL, DiscordLocale.POLISH);

    private final String shortName;
    private final String displayName;
    private final ZoneId timeZone;
    private final CountryCode countryCode;
    private final DiscordLocale discordLocale;
    public static LanguageType fromDiscord(DiscordLocale userLocale) {
        for (LanguageType value : values()) {
            if (value.getDiscordLocale() == userLocale) return value;
        }
        return Static.defualtLanguage.getLanguageType();
    }

}