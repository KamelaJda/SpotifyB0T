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

package me.kamelajda.utils.listener;

import com.google.common.eventbus.EventBus;
import java.util.Map;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.handle.PresenceUpdateHandler;
import net.dv8tion.jda.internal.handle.SocketHandler;
import org.jetbrains.annotations.NotNull;

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
