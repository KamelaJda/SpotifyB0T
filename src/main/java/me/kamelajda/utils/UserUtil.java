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

package me.kamelajda.utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.lang.Nullable;

import java.awt.*;
import java.util.HashSet;
import java.util.Objects;

public class UserUtil {

    public static Color getColor(Member member) {
        return member.getRoles().stream()
            .map(Role::getColor)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(Color.green);
    }

    public static Color getColor(@Nullable Member member, ShardManager sm) {
        if (member != null) return getColor(member);

        JDA jda = sm.getShards().get(Static.RANDOM.nextInt(sm.getShards().size()));
        Guild guild = new HashSet<>(jda.getGuilds()).stream().findAny().orElse(null);

        return guild != null ? getColor(guild.getSelfMember()) : Color.green;
    }

    public static String getName(User u) {
        return u.getAsTag();
    }

    public static String getLogName(User u) {
        return u.getAsMention() + " " + getName(u) + "[" + u.getId() + "]";
    }

    public static String getLogName(Member member) {
        return getLogName(member.getUser());
    }
}
