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
import me.kamelajda.jpa.models.ArtistCreation;
import me.kamelajda.jpa.models.ArtistInfo;
import me.kamelajda.jpa.models.GuildConfig;
import me.kamelajda.jpa.models.UserConfig;
import me.kamelajda.jpa.repository.ArtistCreationRepository;
import me.kamelajda.jpa.repository.ArtistInfoRepository;
import me.kamelajda.utils.enums.CreationType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import java.util.*;

@Slf4j
@Service
public class SubscribeArtistService {

    private final UserConfigService userConfigService;
    private final ArtistInfoRepository artistInfoRepository;
    private final GuildConfigService guildConfigService;
    private final ArtistCreationRepository artistCreationRepository;

    public SubscribeArtistService(UserConfigService userConfigService, ArtistInfoRepository artistInfoRepository, GuildConfigService guildConfigService, ArtistCreationRepository artistCreationRepository) {
        this.userConfigService = userConfigService;
        this.artistInfoRepository = artistInfoRepository;
        this.guildConfigService = guildConfigService;
        this.artistCreationRepository = artistCreationRepository;
    }

    public void addArtistsForUser(Long userId, List<String> artistId, List<Artist> allValues, SpotifyService spotifyService) {
        addArtists(userId, null, artistId, allValues, spotifyService);
    }

    public void addArtistsForGuild(Long guildId, List<String> artistId, List<Artist> allValues, SpotifyService spotifyService) {
        addArtists(null, guildId, artistId, allValues, spotifyService);
    }
    
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
                    AlbumSimplified album = spotifyService.getLastAlbum(s);

                    if (album != null) {
                        ArtistCreation lastAlbum = artistCreationRepository.save(createCreation(CreationType.ALBUM, album));
                        builder.lastAlbum(lastAlbum);
                    }
                } catch (Exception e) {
                    log.error("An error occurred while getting a last album", e);
                }

                try {
                    AlbumSimplified tracks = spotifyService.getLastTrack(artist.getId());
                    if (tracks != null) {
                        ArtistCreation lastTrack = artistCreationRepository.save(createCreation(CreationType.TRACK, tracks));
                        builder.lastTrack(lastTrack);
                    }
                } catch (Exception e) {
                    log.error("An error occurred while getting a last track", e);
                }

                try {
                    AlbumSimplified lastFeat = spotifyService.getLastFeat(artist.getId());
                    if (lastFeat != null) {
                        ArtistCreation lastTrack = artistCreationRepository.save(createCreation(CreationType.FEAT, lastFeat));
                        builder.lastFeat(lastTrack);
                    }
                } catch (Exception e) {
                    log.error("An error occurred while getting a last feat", e);
                }

                return builder.build();
            });

            if (userObject != null) artistInfo.getSubscribeUsers().add(userObject);
            if (guildObject != null) artistInfo.getSubscribeGuilds().add(guildObject);

            artists.add(artistInfo);
        }

        artistInfoRepository.saveAll(artists);
    }

    public static ArtistCreation createCreation(CreationType type, AlbumSimplified item) {
        return ArtistCreation.builder()
            .creationType(type)
            .date(item.getReleaseDate())
            .name(item.getName())
            .link(item.getExternalUrls().get("spotify"))
            .build();
    }

    public void removeArtist(Long userId, String spotifyId) {
        ArtistInfo info = artistInfoRepository.findBySpotifyId(spotifyId).orElseThrow();

        info.getSubscribeUsers().removeIf(o -> o.getUserId().equals(userId));

        artistInfoRepository.save(info);
    }

    public void removeArtistForGuild(Long guildId, String spotifyId) {
        ArtistInfo info = artistInfoRepository.findBySpotifyId(spotifyId).orElseThrow();

        info.getSubscribeGuilds().removeIf(o -> o.getGuildId().equals(guildId));

        artistInfoRepository.save(info);
    }

    public List<ArtistInfo> getAllArtist(UserConfig userConfig) {
        List<ArtistInfo> all = artistInfoRepository.findAllBySubscribeUsers_UserId(userConfig);
        Collections.reverse(all);
        return all;
    }

    public List<ArtistInfo> getAllArtist(GuildConfig guildConfig) {
        List<ArtistInfo> all = artistInfoRepository.findAllBySubscribeGuilds(guildConfig);
        Collections.reverse(all);
        return all;
    }

    public Set<ArtistInfo> getAllArtist(Collection<Long> usersId) {
        return artistInfoRepository.findAllBySubscribeUsers_UserIdIn(usersId);
    }

    public List<ArtistInfo> loadAll() {
        return artistInfoRepository.findAllIsHasSubs();
    }

    public Optional<ArtistInfo> findBySpotifyId(String spotifyId) {
        return artistInfoRepository.findBySpotifyId(spotifyId);
    }

    public void save(ArtistInfo info) {
        artistInfoRepository.save(info);
    }
}
