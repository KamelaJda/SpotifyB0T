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
import me.kamelajda.components.SpotifyB0T;
import me.kamelajda.utils.Static;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@EnableScheduling
@Service
public class StatusService  {

    private final ShardManager shardManager;
    private final String[] games;
    private int customLast = 0;
    private Activity[] customGames;
    private OnlineStatus customStatus;

    private int last = 0;

    public StatusService(SpotifyB0T spotifyB0T, Environment env) {
        this.shardManager = spotifyB0T.getApi();
        this.games = env.getProperty("status.games", String[].class);
    }

    @Scheduled(fixedDelay = 60_000)
    protected void runOneIteration() {
        if ((customGames != null && customGames.length != 0) && customStatus != null) {
            if (customLast >= customGames.length) customLast = 0;
            shardManager.setPresence(customStatus, customGames[customLast]);
            customLast++;
        } else setPresence();
    }

    private void setPresence() {
        if (last >= games.length) last = 0;
        for (JDA jda : shardManager.getShards()) {
            jda.getPresence().setActivity(Activity.playing(games[last]
                .replace("{VERSION}", Static.VERSION)
                .replace("{USERS:ALL}", String.valueOf(fetchUserCount()))
                .replace("{SHARDS}", String.valueOf(shardManager.getShards().size()))
                .replace("{SHARD}", String.valueOf(jda.getShardInfo().getShardId()))
                .replace("{GUILDS:ALL}", shardManager.getGuilds().size() + "")));
        }
        last++;
    }

    private int fetchUserCount() {
        AtomicInteger res = new AtomicInteger();
        shardManager.getShards().forEach(jda -> {
            for (Guild g : jda.getGuilds()) {
                res.addAndGet(g.getMemberCount());
            }
        });
        return res.intValue();
    }

    public void setCustomGame(Activity customGame) {
        setCustomPresence(OnlineStatus.ONLINE, customGame);
    }

    public void setCustomGames(List<Activity> customGame) {
        setCustomPresences(OnlineStatus.ONLINE, customGame);
    }

    public void setCustomGames(Activity... customGame) {
        setCustomPresences(OnlineStatus.ONLINE, customGame);
    }

    public void setCustomPresence(OnlineStatus status, Activity customGame) {
        setCustomPresences(status, customGame == null ? null : Collections.singletonList(customGame));
    }

    public void setCustomPresences(OnlineStatus status, List<Activity> customGame) {
        setCustomPresences(status, customGame == null ? null : customGame.toArray(new Activity[0]));
    }

    public void setCustomPresences(OnlineStatus status, Activity... customGames) {
        if (customGames == null) customGames = new Activity[0];
        customStatus = status;
        customGames = customGames.length == 0 || (customGames.length == 1 && customGames[0] == null) ? null : customGames;
        customLast = 0;
        shardManager.setPresence(status == null ? OnlineStatus.ONLINE : status, customGames.length > 0 ? customGames[0] : null);
        customLast++;
    }

}
