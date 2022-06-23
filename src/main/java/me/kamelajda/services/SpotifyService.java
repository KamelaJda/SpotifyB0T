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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.kamelajda.jpa.models.ArtistCreation;
import me.kamelajda.jpa.models.ArtistInfo;
import me.kamelajda.jpa.models.GuildConfig;
import me.kamelajda.jpa.models.UserConfig;
import me.kamelajda.jpa.repository.ArtistCreationRepository;
import me.kamelajda.utils.enums.CreationType;
import me.kamelajda.utils.language.Language;
import me.kamelajda.utils.language.LanguageService;
import me.kamelajda.utils.language.LanguageType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Paging;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service

public class SpotifyService {

    private final ScheduledExecutorService accessTokenScheduler = Executors.newScheduledThreadPool(1);

    private final ScheduledExecutorService checkAlbumsScheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService executor = Executors.newFixedThreadPool(LanguageType.values().length);

    private final ExecutorService sendMessagesExecutor = Executors.newFixedThreadPool(10);

    private final SpotifyApi api;
    private final SubscribeArtistService subscribeArtistService;
    private final LanguageService languageService;
    private final ArtistCreationRepository artistCreationRepository;

    @Getter @Setter private ShardManager shardManager;

    public SpotifyService(SpotifyApi api, SubscribeArtistService subscribeArtistService, LanguageService languageService, ArtistCreationRepository artistCreationRepository) {
        this.api = api;
        this.subscribeArtistService = subscribeArtistService;
        this.languageService = languageService;
        this.artistCreationRepository = artistCreationRepository;

        refreshAccessToken();
        setupNotification();
    }

    public CompletableFuture<Paging<Artist>> searchArtists(String query) {
        return api.searchArtists(query).limit(10).build().executeAsync();
    }

    public AlbumSimplified getLastAlbum(String artistId) throws IOException, ParseException, SpotifyWebApiException {
        AlbumSimplified[] execute = api.getArtistsAlbums(artistId).offset(0).limit(1).build().execute().getItems();
        if (execute == null || execute.length == 0) return null;
        return execute[0];
    }

    public AlbumSimplified getLastTrack(String artistId) throws IOException, ParseException, SpotifyWebApiException {
        AlbumSimplified[] execute = api.getArtistsAlbums(artistId).album_type("single").limit(1).offset(0).build().execute().getItems();
        if (execute == null || execute.length == 0) return null;
        return execute[0];
    }

    public AlbumSimplified getLastFeat(String artistId) throws IOException, ParseException, SpotifyWebApiException {
        AlbumSimplified[] execute = api.getArtistsAlbums(artistId).album_type("appears_on,single").limit(10).offset(0).build().execute().getItems();
        if (execute == null || execute.length == 0) return null;

        return Arrays.stream(execute).filter(p -> !p.getArtists()[0].getId().equals(artistId)).findFirst().orElse(null);
    }

    public void refreshAccessToken() {
        try {
            ClientCredentials cr = api.clientCredentials().build().execute();
            api.setAccessToken(cr.getAccessToken());
            accessTokenScheduler.schedule(this::refreshAccessToken, cr.getExpiresIn() - 120L, TimeUnit.SECONDS);
        } catch (Exception e) {
            accessTokenScheduler.schedule(this::refreshAccessToken, 60, TimeUnit.SECONDS);
        }
    }
    
    public long timeToRefresh(LanguageType lang) {
        ZonedDateTime now = ZonedDateTime.now(lang.getTimeZone());
        ZonedDateTime nextRun = now.withHour(0).withMinute(10).withSecond(0);

        if (now.compareTo(nextRun) > 0) nextRun = nextRun.plusDays(1);

        return Duration.between(now, nextRun).getSeconds();
    }

    public void setupNotification() {
        checkAlbumsScheduler.scheduleAtFixedRate(
            () -> {
                Set<ArtistInfo> infos = subscribeArtistService.loadAll();

                int index = 0;

                String avatarUrl = getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl();

                for (ArtistInfo info : infos) {
                    executor.execute(() -> {
                        List<Object[]> newCreations = new ArrayList<>();

                        try {
                            configure(info.getLastAlbum(), getLastAlbum(info.getSpotifyId()), CreationType.ALBUM, info, newCreations);
                        } catch (Exception e) {
                            log.error("An error occurred while getting a last album", e);
                        }

                        try {
                            configure(info.getLastTrack(), getLastTrack(info.getSpotifyId()), CreationType.TRACK, info, newCreations);
                        } catch (Exception e) {
                            log.error("An error occurred while getting a last track", e);
                        }

                        try {
                            configure(info.getLastFeat(), getLastFeat(info.getSpotifyId()), CreationType.FEAT, info, newCreations);
                        } catch (Exception e) {
                            log.error("An error occurred while getting a last track", e);
                        }

                        if (newCreations.isEmpty()) return;

                        try {
                            subscribeArtistService.save(info);

                            for (GuildConfig guild : info.getSubscribeGuilds().stream().filter(f -> f.getNotificationChannelId() != null).collect(Collectors.toSet())) {
                                sendMessagesExecutor.execute(() -> {
                                    Language l = languageService.get(guild.getLanguage());

                                    List<MessageEmbed> embeds = embeds(l, info, newCreations, avatarUrl);

                                    try {
                                        Guild guildImpl = shardManager.getGuildById(guild.getGuildId());
                                        if (guildImpl == null) return;

                                        TextChannel channel = guildImpl.getTextChannelById(guild.getNotificationChannelId());

                                        if (channel == null || !channel.canTalk()) {
                                            User owner = guildImpl.getOwner().getUser();
                                            owner.openPrivateChannel().complete().sendMessage(l.get("spotify.service.notification.senderror", guildImpl.getName())).queue();
                                            return;
                                        }

                                        channel.sendMessageEmbeds(embeds).queue();
                                    } catch (Exception ignored) { }
                                });
                            }

                            for (UserConfig user : info.getSubscribeUsers()) {
                                sendMessagesExecutor.execute(() -> {
                                    Language l = languageService.get(user.getLanguageType());
                                    List<MessageEmbed> embeds = embeds(l, info, newCreations, avatarUrl);

                                    try {
                                        User u = getShardManager().retrieveUserById(user.getUserId()).complete();
                                        PrivateChannel channel = u.openPrivateChannel().complete();
                                        channel.sendMessageEmbeds(embeds).complete();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    if (++index >= 10) {
                        try {
                            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                            index = 0;
                        } catch (InterruptedException ignored) { }
                    }
                }
            }, timeToRefresh(LanguageType.POLISH), TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS
        );
    }

    public ArtistCreation isNew(ArtistCreation old, AlbumSimplified maybeNew, CreationType creationType) {
        if (old.getLink().equals(maybeNew.getExternalUrls().get("spotify")) || old.getName().equals(maybeNew.getName())) return null;

        return artistCreationRepository.save(SubscribeArtistService.createCreation(creationType, maybeNew));
    }

    public void configure(ArtistCreation old, AlbumSimplified maybeNew, CreationType creationType, ArtistInfo info, List<Object[]> list) {
        if (maybeNew == null) return;
        try {
            ArtistCreation isNew = isNew(old, maybeNew, creationType);
            if (isNew == null) return;

            switch (creationType) {
                case ALBUM:
                    info.setLastAlbum(isNew);
                    break;
                case TRACK:
                    info.setLastTrack(isNew);
                    break;
                case FEAT:
                    info.setLastFeat(isNew);
                    break;
                default: log.error("CreationType {} is invalid!", creationType);
            }

            list.add(new Object[] {maybeNew, isNew});
        } catch (Exception e) {
            log.error("An error occurred while getting a last album", e);
        }
    }

    public List<MessageEmbed> embeds(Language l, ArtistInfo info, List<Object[]> objects, String avatarUrl) {
        List<MessageEmbed> embeds = new ArrayList<>();

        for (Object[] object : objects) {
            embeds.add(embed(l, info, (AlbumSimplified) object[0], avatarUrl, (ArtistCreation) object[1]));
        }

        return embeds;
    }

    public MessageEmbed embed(Language l, ArtistInfo info, AlbumSimplified newAlbum, String avatarUrl, ArtistCreation creation) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.BLUE);
        eb.setTitle(l.get("spotify.service.notification.new." + creation.getCreationType().name().toLowerCase()));
        eb.setDescription(l.get("spotify.service.notification.description",
            l.get("spotify.service.notification.type." + creation.getCreationType().name().toLowerCase()),
            info.getDisplayName(),
            info.getLink(),
            newAlbum.getName(),
            newAlbum.getExternalUrls().get("spotify")));

        if (newAlbum.getImages().length > 0) eb.setImage(newAlbum.getImages()[0].getUrl());

        eb.setTimestamp(Instant.now());
        eb.setFooter("SpotifyB0T", avatarUrl);

        return eb.build();
    }

}