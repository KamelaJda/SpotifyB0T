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

package me.kamelajda.utils.module;

import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ModuleManager {

    public final Set<IModule> modules = new HashSet<>();

    public void loadModule(IModule module) {
        if (modules.stream().filter(f -> f.getName().equals(module.getName())).count() > 1) {
            log.error("Module with name " + module.getName() + " already exist");
            return;
        }

        log.info("Loading module " + module.getName() + "...");
        modules.add(module);

        try {
            module.startUp();
            log.info("Module " + module.getName() + " is successfully loaded!");
        } catch (Exception e) {
            log.error("An error occurred while enabling module: " + module.getName(), e);
        }
    }
}
