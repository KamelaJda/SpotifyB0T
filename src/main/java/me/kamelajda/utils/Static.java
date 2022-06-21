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

import me.kamelajda.MainApplication;
import me.kamelajda.utils.language.Language;

import java.util.Date;
import java.util.Optional;
import java.util.Random;

public class Static {

    public static final Random RANDOM = new Random();
    public static final int MAX_SUBSCRIPTIONS = 50;
    public static final String VERSION;
    public static final Date START_DATE;

    public static Language defualtLanguage = null;

    static {
        START_DATE = new Date();

        String v = "?.?.?";

        try {
            v = Optional.of(MainApplication.class.getPackage().getImplementationVersion()).orElse("?.?.?");
        } catch (Exception ignored) { }

        VERSION = v;
    }

}
