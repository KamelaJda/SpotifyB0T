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
import lombok.RequiredArgsConstructor;
import me.kamelajda.jpa.models.ArtistInfo;
import me.kamelajda.jpa.models.UserConfig;
import me.kamelajda.modules.commands.commands.ArtistsCommand;
import me.kamelajda.modules.commands.commands.SubscribeCommand;
import me.kamelajda.services.GuildConfigService;
import me.kamelajda.services.SubscribeArtistService;
import me.kamelajda.services.UserConfigService;
import me.kamelajda.utils.EmbedPaginator;
import me.kamelajda.utils.EventWaiter;
import me.kamelajda.utils.Listener;
import me.kamelajda.utils.language.Language;
import me.kamelajda.utils.language.LanguageService;
import me.kamelajda.utils.language.LanguageType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ExecuteCommandAsButtonListener implements Listener {

    private final SubscribeArtistService subscribeArtistService;
    private final EventWaiter eventWaiter;
    private final UserConfigService userConfigService;
    private final LanguageService languageService;
    private final GuildConfigService guildConfigService;

    @Subscribe
    public void onButtonClick(ButtonInteractionEvent e) {
        if (!e.getComponentId().startsWith("executecommand-")) return;

        String rawCmd = e.getComponentId().split("executecommand-")[1];

        UserConfig load = userConfigService.load(e.getUser().getIdLong());

        Language language = languageService.get(LanguageType.fromDiscord(e.getUserLocale()));

        if (rawCmd.equals("artists-" + SubscribeCommand.SubscribeType.SERVER.name().toLowerCase()) || rawCmd.equals("artists-" + SubscribeCommand.SubscribeType.PRIVATE.name().toLowerCase())) {
            e.deferReply(false).queue();
            e.getHook().editOriginal(language.get("global.generic.loading")).queue();

            boolean forServer = e.getComponentId().endsWith(SubscribeCommand.SubscribeType.SERVER.name().toLowerCase());

            List<ArtistInfo> list = !forServer ? subscribeArtistService.getAllArtist(load) : subscribeArtistService.getAllArtist(guildConfigService.load(e.getGuild().getIdLong()));

            if (list == null || list.isEmpty()) {
                e.getHook().editOriginal(language.get("artists.empty")).queue();
                return;
            }

            String name = forServer ? Objects.requireNonNull(e.getGuild()).getName() : e.getUser().getAsMention();

            List<EmbedBuilder> pages = list.stream()
                .map(m -> ArtistsCommand.embed(language, e.getMember(), m, forServer ? SubscribeCommand.SubscribeType.SERVER : SubscribeCommand.SubscribeType.PRIVATE, name))
                .collect(Collectors.toList());

            EmbedPaginator.create(pages,
                e.getUser(), eventWaiter, e.getHook(),
                ArtistsCommand.formatButton(language, forServer),
                ArtistsCommand.consumer(subscribeArtistService, e.getUser())
            );

        } else {
            e.deferReply(true).queue();
            e.getHook().editOriginal("Nie znaleziono takiej komendy!").queue();
        }
    }
}
