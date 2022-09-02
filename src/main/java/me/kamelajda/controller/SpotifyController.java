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

package me.kamelajda.controller;

import me.kamelajda.jpa.models.ArtistInfo;
import me.kamelajda.redis.data.RedisSpotifyState;
import me.kamelajda.redis.services.RedisStateService;
import me.kamelajda.services.SpotifyService;
import me.kamelajda.services.SubscribeArtistService;
import me.kamelajda.services.UserConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class SpotifyController {

    private final SpotifyService spotifyService;
    private final SubscribeArtistService subscribeArtistService;
    private final RedisStateService redisStateService;
    private final UserConfigService userConfigService;

    public SpotifyController(SpotifyService spotifyService, SubscribeArtistService subscribeArtistService, RedisStateService redisStateService, UserConfigService userConfigService) {
        this.spotifyService = spotifyService;
        this.subscribeArtistService = subscribeArtistService;
        this.redisStateService = redisStateService;
        this.userConfigService = userConfigService;
    }

    @GetMapping("/connect")
    public String connectSpotifyAccount(@RequestParam String code, @RequestParam String state) {
        try {
            RedisSpotifyState spotifyState = redisStateService.findValueById(state).orElseThrow();

            List<Artist> artists = spotifyService.getSubscribedArtists(code);

            Set<String> subscribedArtists = subscribeArtistService
                .getAllArtist(userConfigService.load(spotifyState.getUserId())).stream().map(ArtistInfo::getSpotifyId)
                .collect(Collectors.toSet());

            artists.removeIf(a -> subscribedArtists.contains(a.getId()));

            List<String> artistsIds = artists.stream().map(Artist::getId).collect(Collectors.toList());

            subscribeArtistService.addArtistsForUser(spotifyState.getUserId(), artistsIds, artists, spotifyService);

            return "All Completed. You can close it";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error!";
        }
    }

}
