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

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LanguageService {

    private final Map<LanguageType, Language> languages = new HashMap<>();

    public LanguageService() {
        for (LanguageType value : LanguageType.values()) {
            Language language = new Language(value);
            language.loadMessages();
            languages.put(value, language);
        }
    }

    public Language get(LanguageType lang) {
        return languages.getOrDefault(lang, languages.values().stream().findAny().orElseThrow());
    }
}
