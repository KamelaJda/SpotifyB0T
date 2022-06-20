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

package me.kamelajda.modules.commands.commands;

import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import me.kamelajda.jpa.models.ArtistInfo;
import me.kamelajda.services.SpotifyService;
import me.kamelajda.services.SubscribeArtistService;
import me.kamelajda.utils.EventWaiter;
import me.kamelajda.utils.commands.ICommand;
import me.kamelajda.utils.commands.SlashContext;
import me.kamelajda.utils.enums.CommandCategory;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Paging;

public class SubscribeCommand extends ICommand {

  private final SubscribeArtistService subscribeArtistService;
  private final SpotifyService spotifyService;
  private final EventWaiter eventWaiter;

  public SubscribeCommand(SubscribeArtistService subscribeArtistService, SpotifyService spotifyService, EventWaiter eventWaiter) {
      this.subscribeArtistService = subscribeArtistService;
      this.spotifyService = spotifyService;
      this.eventWaiter = eventWaiter;

      name = "subscribe";
      category = CommandCategory.BASIC;
      commandData = getData().addOptions(new OptionData(OptionType.STRING, "artist", "Artist name", true));
  }

  @Override
  protected boolean execute(SlashContext context) {
    context.getEvent().deferReply(true).queue();
    String artist = Objects.requireNonNull(context.getEvent().getOption("artist")).getAsString();

    context.sendTranslate("subscribe.search.start");

    CompletableFuture<Paging<Artist>> future = spotifyService.searchArtists(artist);

    future.whenComplete(
        (req, th) -> {
          if (th != null) {
            context.sendTranslate("subscribe.artists.error");
            th.printStackTrace();
            return;
          }

          Set<String> subscribeArtist =
              subscribeArtistService.getAllArtist(context.getUserConfig()).stream()
                  .map(ArtistInfo::getSpotifyId)
                  .collect(Collectors.toSet());

          List<Artist> items = Arrays.asList(req.getItems());

          if (items.isEmpty()) {
            context.sendTranslate("subscribe.artists.not.found");
            return;
          }

          String componentId = context.getUser().getId() + "-choose-artists";

          SelectMenu.Builder builder =
              SelectMenu.create(componentId)
                  .setPlaceholder(context.getLanguage().get("subscribe.artists.choose"));
          builder.setMinValues(1).setMaxValues(items.size());

          for (Artist s : items) {
            if (subscribeArtist.contains(s.getId())) continue;

            if (s.getName().length() > 100)
              builder.addOption(s.getName().substring(0, 100), s.getId());
            else builder.addOption(s.getName(), s.getId());
          }

          if (builder.getOptions().isEmpty()) {
            context.sendTranslate("subscribe.all.already.subscribed");
            return;
          }

          context
              .getHook()
              .editOriginal(context.getLanguage().get("subscribe.choose.artists"))
              .setActionRow(builder.build())
              .queue();

          eventWaiter.waitForEvent(
              SelectMenuInteractionEvent.class,
              e -> e.getComponentId().equals(componentId),
              e -> {
                e.deferEdit().queue();

                List<SelectOption> options = e.getSelectedOptions();

                subscribeArtistService.addArtistsForUser(context.getUser().getIdLong(), options.stream().map(SelectOption::getValue).collect(Collectors.toList()), List.of(req.getItems()), spotifyService);

                context.getHook().editOriginalComponents(Collections.emptyList()).queue();

                MessageBuilder messageBuilder =
                    new MessageBuilder(
                        context.getLanguage().get("subscribe.success.subscribed", options.size()));
                messageBuilder.setActionRows(
                    ActionRow.of(
                        Button.success(
                            "executecommand-artists", "Wyświetl subskrybowanych artystów")));

                context.getHook().editOriginal(messageBuilder.build()).queue();
              },
              30,
              TimeUnit.SECONDS,
              () ->
                  context
                      .getHook()
                      .editOriginal(context.getLanguage().get("global.timeout"))
                      .setActionRow(Collections.emptyList())
                      .queue());
        });

    return true;
  }
}
