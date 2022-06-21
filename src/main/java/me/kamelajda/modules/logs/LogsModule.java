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

package me.kamelajda.modules.logs;

import com.google.common.eventbus.EventBus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.kamelajda.modules.logs.listeners.CommandExecuteListener;
import me.kamelajda.utils.module.IModule;
import org.springframework.core.env.Environment;

@RequiredArgsConstructor
@Getter
public class LogsModule extends IModule {

    private final String name = "logs";
    private final EventBus eventBus;

    private final Environment environment;

    @Override
    public void startUp() {
        registerListener(new CommandExecuteListener(environment));
    }

    @Override
    public void disable() {
        unregisterListeners();
    }

}
