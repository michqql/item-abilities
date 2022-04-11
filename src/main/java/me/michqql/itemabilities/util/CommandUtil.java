package me.michqql.itemabilities.util;

import me.michqql.core.util.OfflineUUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CommandUtil {

    public static int parseInt(String[] args, int index, int def) {
        if(index < 0 || args.length < index + 1)
            return def;

        try {
            return Integer.parseInt(args[index]);
        } catch(NumberFormatException e) {
            return def;
        }
    }

    public static Player parseOnlinePlayer(String[] args, int index) {
        if(index < 0 || args.length < index + 1)
            return null;

        return Bukkit.getPlayer(args[index]);
    }

    /**
     * Recommended running async
     */
    public static OfflinePlayer parseOfflinePlayer(String[] args, int index) {
        if(index < 0 || args.length < index + 1)
            return null;

        String playerName = args[index];
        UUID uuid = OfflineUUID.getUUID(playerName);
        if(uuid == null)
            return null;

        return Bukkit.getOfflinePlayer(uuid);
    }

    public static List<String> matchPlayerNames(String start) {
        start = start.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();

        OfflinePlayer[] players = Bukkit.getOfflinePlayers();
        for(OfflinePlayer player : players) {
            if(player == null || !player.hasPlayedBefore() || player.getName() == null)
                continue;

            if(player.getName().toLowerCase(Locale.ROOT).startsWith(start))
                result.add(player.getName());
        }
        return result;
    }
}
