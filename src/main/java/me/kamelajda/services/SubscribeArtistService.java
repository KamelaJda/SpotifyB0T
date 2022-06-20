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

package me.kamelajda.services;

import lombok.extern.slf4j.Slf4j;
import me.kamelajda.jpa.models.ArtistInfo;
import me.kamelajda.jpa.models.GuildConfig;
import me.kamelajda.jpa.models.UserConfig;
import me.kamelajda.jpa.repository.ArtistInfoRepository;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Paging;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional
public class SubscribeArtistService {

    private final UserConfigService userConfigService;
    private final ArtistInfoRepository artistInfoRepository;
    private final GuildConfigService guildConfigService;

    public SubscribeArtistService(UserConfigService userConfigService, ArtistInfoRepository artistInfoRepository, GuildConfigService guildConfigService) {
        this.userConfigService = userConfigService;
        this.artistInfoRepository = artistInfoRepository;
        this.guildConfigService = guildConfigService;
    }

    @Transactional
    public void addArtistsForUser(Long userId, List<String> artistId, List<Artist> allValues, SpotifyService spotifyService) {
        addArtists(userId, null, artistId, allValues, spotifyService);
    }

    @Transactional
    public void addArtistsForGuild(Long guildId, List<String> artistId, List<Artist> allValues, SpotifyService spotifyService) {
        addArtists(null, guildId, artistId, allValues, spotifyService);
    }

    @Transactional
    public void addArtists(@Nullable Long userId, @Nullable Long guildId, List<String> artistId, List<Artist> allValues, SpotifyService spotifyService) {
        Set<ArtistInfo> artists = new HashSet<>();

        UserConfig userObject = userId != null ? userConfigService.load(userId) : null;
        GuildConfig guildObject = guildId != null ? guildConfigService.load(guildId) : null;

        for (String s : artistId) {
            Artist artist = allValues.stream().filter(f -> f.getId().equals(s)).findFirst().orElse(null);

            if (artist == null) continue;

            ArtistInfo artistInfo = artistInfoRepository.findBySpotifyId(s).orElseGet(() -> {
                ArtistInfo.ArtistInfoBuilder builder =
                    ArtistInfo.builder().spotifyId(s).displayName(artist.getName())
                        .thumbnailUrl(artist.getImages().length > 0 ? artist.getImages()[0].getUrl() : null)
                        .link(artist.getExternalUrls().get("spotify"));

                try {
                    Paging<AlbumSimplified> album = spotifyService.getLastAlbum(s);

                    if (album.getItems().length > 0) {
                        AlbumSimplified item = album.getItems()[0];
                        builder.lastAlbumDate(item.getReleaseDate());
                        builder.lastAlbumName(item.getName());
                        builder.lastAlbumLink(item.getExternalUrls().get("spotify"));
                    }

                } catch (Exception e) {
                    log.error("An error occurred while getting a last album", e);
                }

                return builder.build();
            });

            if (userObject != null) artistInfo.getSubscribeUsers().add(userObject);
            if (guildObject != null) artistInfo.getSubscribeUsers().add(userObject);

            artists.add(artistInfo);
        }

        artistInfoRepository.saveAll(artists);
    }

    @Transactional
    public void removeArtist(Long userId, String spotifyId) {
        UserConfig object = userConfigService.load(userId);

        ArtistInfo info = artistInfoRepository.findBySpotifyId(spotifyId).orElseThrow();
        info.getSubscribeUsers().remove(object);

        artistInfoRepository.save(info);
    }

    @Transactional
    public List<ArtistInfo> getAllArtist(UserConfig userConfig) {
        return artistInfoRepository.findAllBySubscribeUsers_UserId(userConfig);
    }

    @Transactional
    public Set<ArtistInfo> getAllArtist(Collection<Long> usersId) {
        return artistInfoRepository.findAllBySubscribeUsers_UserIdIn(usersId);
    }

    public void save(ArtistInfo info) {
        artistInfoRepository.save(info);
    }
}
