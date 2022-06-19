package me.kamelajda;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import me.kamelajda.utils.EventBusErrorHandler;
import me.kamelajda.utils.EventWaiter;
import okhttp3.OkHttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import se.michaelthelin.spotify.SpotifyApi;

import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
public class MainApplication {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder().build();
    }

    @Bean
    public SpotifyApi spotifyApi(Environment env) {
        return new SpotifyApi.Builder()
                .setClientId(env.getProperty("spotify.client.id"))
                .setClientSecret(env.getProperty("spotify.client.secret"))
                .build();
    }

    @Bean
    public EventBus eventBus() {
        return new AsyncEventBus(Executors.newFixedThreadPool(16), EventBusErrorHandler.instance);
    }

    @Bean
    public EventWaiter eventWaiter() {
        return new EventWaiter();
    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

}
