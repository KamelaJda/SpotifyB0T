package me.kamelajda.utils.module;

import com.google.common.eventbus.EventBus;
import lombok.Getter;
import lombok.Setter;
import me.kamelajda.utils.Listener;

import java.util.HashSet;
import java.util.Set;

public abstract class IModule {

    private final Set<Listener> listeners = new HashSet<>();

    @Getter @Setter
    private boolean enabled;

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
