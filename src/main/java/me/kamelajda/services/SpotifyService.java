package me.kamelajda.services;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.kamelajda.jpa.models.ArtistInfo;
import me.kamelajda.jpa.models.UserConfig;
import me.kamelajda.utils.language.Language;
import me.kamelajda.utils.language.LanguageService;
import me.kamelajda.utils.language.LanguageType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
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
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SpotifyService {

    private final ScheduledExecutorService accessTokenScheduler = Executors.newScheduledThreadPool(1);

    private final ScheduledExecutorService checkAlbumsCheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService executor = Executors.newFixedThreadPool(LanguageType.values().length);

    private final SpotifyApi api;
    private final UserConfigService userConfigService;
    private final SubscribeArtistService subscribeArtistService;
    private final LanguageService languageService;

    @Getter @Setter
    private ShardManager shardManager;

    public SpotifyService(SpotifyApi api, UserConfigService userConfigService, SubscribeArtistService subscribeArtistService, LanguageService languageService) {
        this.api = api;
        this.userConfigService = userConfigService;
        this.subscribeArtistService = subscribeArtistService;
        this.languageService = languageService;

        refreshAccessToken();
        setupNotification();
    }

    public CompletableFuture<Paging<Artist>> searchArtists(String query) {
        return api.searchArtists(query).limit(10).build().executeAsync();
    }

    public Paging<AlbumSimplified> getLastAlbum(String artistId) throws IOException, ParseException, SpotifyWebApiException {
        return api.getArtistsAlbums(artistId).offset(0).limit(1).build().execute();
    }

    private void refreshAccessToken() {
        try {
            ClientCredentials cr = api.clientCredentials().build().execute();
            api.setAccessToken(cr.getAccessToken());
            accessTokenScheduler.schedule(this::refreshAccessToken, cr.getExpiresIn() - 120L, TimeUnit.SECONDS);
        } catch (Exception e) {
            accessTokenScheduler.schedule(this::refreshAccessToken, 60, TimeUnit.SECONDS);
        }
    }

    private long timeToRefresh(LanguageType lang) {
        ZonedDateTime now = ZonedDateTime.now(lang.getTimeZone());
        ZonedDateTime nextRun = now.withHour(0).withMinute(10).withSecond(0);

        if (now.compareTo(nextRun) > 0) nextRun = nextRun.plusDays(1);

        return Duration.between(now, nextRun).getSeconds();
    }

    private void setupNotification() {
        checkAlbumsCheduler.scheduleAtFixedRate(() -> {
            List<UserConfig> list = userConfigService.getAll();

            Set<ArtistInfo> infos = subscribeArtistService.getAllArtist(list.stream().map(UserConfig::getUserId).collect(Collectors.toSet()));

            int index = 0;
            for (ArtistInfo info : infos) {
                executor.execute(() -> {
                    try {
                        AlbumSimplified[] album = getLastAlbum(info.getSpotifyId()).getItems();
                        if (album.length == 0) return;

                        AlbumSimplified newAlbum = album[0];

                        if (newAlbum.getExternalUrls().get("spotify").equals(info.getLastAlbumLink())) {
                            return;
                        }

                        info.setLastAlbumDate(newAlbum.getReleaseDate());
                        info.setLastAlbumLink(newAlbum.getExternalUrls().get("spotify"));
                        info.setLastAlbumName(newAlbum.getName());

                        subscribeArtistService.save(info);

                        for (UserConfig user : info.getSubscribeUsers()) {
                            Language l = languageService.get(user.getLanguageType());

                            EmbedBuilder eb = new EmbedBuilder();
                            eb.setColor(Color.BLUE);
                            eb.setTitle(l.get("spotify.service.notification.new.album"));
                            eb.setDescription(l.get("spotify.service.notification.description", info.getDisplayName(), info.getLink(), info.getLastAlbumName(), info.getLastAlbumLink()));
                            if (newAlbum.getImages().length > 0) eb.setImage(newAlbum.getImages()[0].getUrl());

                            eb.setTimestamp(Instant.now());
                            eb.setFooter("SpotifyB0T", getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl());

                            try {
                                User u = getShardManager().retrieveUserById(user.getUserId()).complete();
                                PrivateChannel channel = u.openPrivateChannel().complete();
                                channel.sendMessageEmbeds(eb.build()).complete();
                            } catch (Exception ignored) { }
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

        }, timeToRefresh(LanguageType.POLISH), TimeUnit.HOURS.toSeconds(5), TimeUnit.SECONDS);
    }

}
