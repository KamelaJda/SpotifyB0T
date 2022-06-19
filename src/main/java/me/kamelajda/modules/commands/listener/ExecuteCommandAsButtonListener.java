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

package me.kamelajda.modules.commands.listener;

import com.google.common.eventbus.Subscribe;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.kamelajda.jpa.models.ArtistInfo;
import me.kamelajda.modules.commands.commands.ArtistsCommand;
import me.kamelajda.services.SubscribeArtistService;
import me.kamelajda.utils.EmbedPaginator;
import me.kamelajda.utils.EventWaiter;
import me.kamelajda.utils.Listener;
import me.kamelajda.utils.Static;
import me.kamelajda.utils.language.Language;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@RequiredArgsConstructor
public class ExecuteCommandAsButtonListener implements Listener {

  private final SubscribeArtistService subscribeArtistService;
  private final EventWaiter eventWaiter;

  @Subscribe
  public void onButtonClick(ButtonInteractionEvent e) {
    if (!e.getComponentId().startsWith("executecommand-")) return;

    String rawCmd = e.getComponentId().split("executecommand-")[1];

    // TODO
    Language language = Static.defualtLanguage;

    if (rawCmd.equals("artists")) {
      e.deferReply(false).queue();

      e.reply(language.get("global.generic.loading")).queue();

      List<ArtistInfo> list = subscribeArtistService.getAllArtist(e.getUser().getIdLong());

      if (list == null || list.isEmpty()) {
        e.reply(language.get("artists.empty")).queue();
        return;
      }

      List<EmbedBuilder> pages =
          list.stream()
              .map(m -> ArtistsCommand.embed(language, e.getMember(), m))
              .collect(Collectors.toList());

      EmbedPaginator.create(pages, e.getUser(), eventWaiter, e.getHook());
    } else {
      e.deferReply(true).queue();
      e.reply("Nie znaleziono takiej komendy!").queue();
    }
  }
}
