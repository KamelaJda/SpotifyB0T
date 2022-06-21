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

import com.google.common.util.concurrent.AbstractScheduledService;
import lombok.extern.slf4j.Slf4j;
import me.kamelajda.utils.Static;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.core.env.Environment;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class StatusService extends AbstractScheduledService  {

    private static ShardManager shardManager;
    private static String[] games;
    private static int customLast = 0;
    private static Activity[] customGames;
    private static OnlineStatus customStatus;

    private int last = 0;

    @SuppressWarnings("squid:S3010")
    public StatusService(ShardManager shardManager, Environment env) {
        StatusService.shardManager = shardManager;
        StatusService.games = env.getProperty("status.games", String[].class);
    }

    @Override
    protected void runOneIteration() {
        log.debug("Czas na zmiane statusu...");
        if ((customGames != null && customGames.length != 0) && customStatus != null) {
            if (customLast >= customGames.length) customLast = 0;
            shardManager.setPresence(customStatus, customGames[customLast]);
            customLast++;
        } else setPresence();
    }

    private void setPresence() {
        if (last >= StatusService.games.length) last = 0;
        for (JDA jda : shardManager.getShards()) {
            jda.getPresence().setActivity(Activity.playing(StatusService.games[last]
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

    public static void setCustomGame(Activity customGame) {
        setCustomPresence(OnlineStatus.ONLINE, customGame);
    }

    public static void setCustomGames(List<Activity> customGame) {
        setCustomPresences(OnlineStatus.ONLINE, customGame);
    }

    public static void setCustomGames(Activity... customGame) {
        setCustomPresences(OnlineStatus.ONLINE, customGame);
    }

    public static void setCustomPresence(OnlineStatus status, Activity customGame) {
        setCustomPresences(status, customGame == null ? null : Collections.singletonList(customGame));
    }

    public static void setCustomPresences(OnlineStatus status, List<Activity> customGame) {
        setCustomPresences(status, customGame == null ? null : customGame.toArray(new Activity[0]));
    }

    public static void setCustomPresences(OnlineStatus status, Activity... customGames) {
        if (customGames == null) customGames = new Activity[0];
        StatusService.customStatus = status;
        StatusService.customGames = customGames.length == 0 || (customGames.length == 1 && customGames[0] == null) ? null : customGames;
        customLast = 0;
        shardManager.setPresence(status == null ? OnlineStatus.ONLINE : status, customGames.length > 0 ? customGames[0] : null);
        customLast++;
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(1, 1, TimeUnit.MINUTES);
    }

}
