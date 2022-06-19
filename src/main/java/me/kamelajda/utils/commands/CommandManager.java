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

import java.lang.reflect.Method;
import java.util.*;

@Getter
@Slf4j
public class CommandManager {

    public final Set<ICommand> registered;
    public final Map<String, ICommand> commands;

    public CommandManager() {
        this.commands = new HashMap<>();
        this.registered = new HashSet<>();
    }

    public void registerCommand(ICommand command) {
        if (command == null) return;

        if (commands.containsKey(command.getName()))  {
            log.error("Command with name {} ({}) has already registered!", command.getName(), command.getClass().getName());
            return;
        }

        if (command.getName() == null || command.getName().isEmpty()) {
            log.error("Command name {} is empty!", command.getClass().getName());
            return;
        }

        for (Method method : command.getClass().getMethods()) {
            try {
                if (method.isAnnotationPresent(SubCommand.class) && method.getParameterCount() == 1) {
                    SubCommand subCommand = method.getAnnotation(SubCommand.class);
                    String name = subCommand.name().isEmpty() ? method.getName() : subCommand.name();
                    command.getSubCommands().put(name.toLowerCase(), method);
                }
            } catch (Exception e) {
                log.error("An error occurred while logging a subcommand", e);
                e.printStackTrace();
            }
        }

        registered.add(command);
        commands.put(command.getName(), command);
        log.debug("Command {} has been registered", command.getName());
    }

    public void unregisterCommands(List<ICommand> cmds) {
        for (ICommand command : cmds) {
            commands.values().removeIf(cmd -> command == cmd || cmd.getName().equals(command.getName()));
            registered.removeIf(cmd -> command == cmd || cmd.getName().equals(command.getName()));
        }
    }

}
