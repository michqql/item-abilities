package me.michqql.itemabilities.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandUtil {

    public static int parseInt(String[] args, int index, int def) {
        if(args.length < index + 1)
            return def;

        try {
            return Integer.parseInt(args[index]);
        } catch(NumberFormatException e) {
            return def;
        }
    }

    public static Player parseOnlinePlayer(String[] args, int index) {
        if(args.length < index + 1)
            return null;

        return Bukkit.getPlayer(args[index]);
    }
}
