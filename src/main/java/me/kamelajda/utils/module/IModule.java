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

import com.google.common.eventbus.EventBus;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import me.kamelajda.utils.Listener;

public abstract class IModule {

    private final Set<Listener> listeners = new HashSet<>();

    @Getter @Setter private boolean enabled;

    protected abstract String getName();

    public abstract EventBus getEventBus();

    public void startUp() {
        throw new UnsupportedOperationException("Not supported");
    }

    public void disable() {
        throw new UnsupportedOperationException("Not supported");
    }

    public void registerListener(Listener listener) {
        listeners.add(listener);
        getEventBus().register(listener);
    }

    public void unregisterListeners() {
        for (Listener listener : listeners) {
            getEventBus().unregister(listener);
        }
        listeners.clear();
    }
}
