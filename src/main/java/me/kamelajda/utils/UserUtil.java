package me.kamelajda.utils;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.Objects;

public class UserUtil {

    public static Color getColor(Member member) {
        return member.getRoles().stream().map(Role::getColor).filter(Objects::nonNull).findAny().orElse(Color.green);
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