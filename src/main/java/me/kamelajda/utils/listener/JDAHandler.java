package me.kamelajda.utils.listener;

import com.google.common.eventbus.EventBus;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.handle.PresenceUpdateHandler;
import net.dv8tion.jda.internal.handle.SocketHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@AllArgsConstructor
public class JDAHandler implements EventListener {

    private final EventBus eventBus;

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            Map<String, SocketHandler> handlers = ((JDAImpl) event.getJDA()).getClient().getHandlers();
            handlers.put("PRESENCE_UPDATE", new PresenceUpdateHandler((JDAImpl) event.getJDA()));
        } else if (event instanceof MessageReceivedEvent) {
            eventBus.post(event);
        } else eventBus.post(event);
    }

}
