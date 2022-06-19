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

package me.kamelajda;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import me.kamelajda.utils.EventBusErrorHandler;
import me.kamelajda.utils.EventWaiter;
import okhttp3.OkHttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import se.michaelthelin.spotify.SpotifyApi;

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
