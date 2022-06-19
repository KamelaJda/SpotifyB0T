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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.kamelajda.utils.Static;
import me.kamelajda.utils.enums.CommandCategory;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

@Getter
@Slf4j
public abstract class ICommand {

    protected String name;
    protected CommandCategory category;
    protected CommandDataImpl commandData = null;

    protected final Map<String, Method> subCommands = new HashMap<>();

    public void preExecute(SlashContext context) {
        execute(context);
    }

    protected boolean execute(SlashContext context) {
        throw new UnsupportedOperationException("Komenda nie ma zaimplementowanej funkcji execute(SlashContext)");
    }

    @Override
    public String toString() {
        return this.name;
    }

    protected CommandDataImpl getData() {
        return new CommandDataImpl(this.name, Static.defualtLanguage.get(this.name + ".description"));
    }
}
