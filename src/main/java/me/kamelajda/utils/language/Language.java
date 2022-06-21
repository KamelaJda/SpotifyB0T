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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.MissingFormatArgumentException;
import java.util.Properties;

@Slf4j
@RequiredArgsConstructor
public class Language {

    @Getter
    private final LanguageType languageType;

    private Properties property = null;

    public void loadMessages() {
        Properties p = new Properties();

        InputStream stream =
                Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream("language/messages-" + languageType.getShortName() + ".properties");

        if (stream == null) {
            log.error("Cannot load language " + languageType + "!");
            return;
        }

        try {
            p.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            property = p;
        } catch (IOException e) {
            log.error("Cannot load language", e);
        }
    }

    public String get(String key) {
        if (property != null && property.containsKey(key)) return property.getProperty(key, key);
        return key;
    }

    public String get(String key, String... toReplace) {
        return get(key, (Object[]) toReplace);
    }

    public String get(String key, Object... toReplace) {
        ArrayList<String> parsedArray = new ArrayList<>();
        for (Object k : toReplace) parsedArray.add(k.toString());
        try {
            return String.format(get(key), parsedArray.toArray());
        } catch (MissingFormatArgumentException e) {
            return get(key);
        }
    }
}
