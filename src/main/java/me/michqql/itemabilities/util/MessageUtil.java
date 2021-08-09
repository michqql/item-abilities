package me.michqql.itemabilities.util;

import me.michqql.itemabilities.data.YamlFile;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-zA-Z0-9]{6}", Pattern.CASE_INSENSITIVE);

    public static String format(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        while(matcher.find()) {
            String hex = input.substring(matcher.start(), matcher.end());
            input = input.replace(hex, ChatColor.of(hex) + "");
            matcher = HEX_PATTERN.matcher(input);
        }
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static List<String> format(List<String> input) {
        List<String> formatted = new ArrayList<>();
        for(String s : input) {
            formatted.add(format(s));
        }
        return formatted;
    }

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%[a-zA-Z.-]*%");

    private FileConfiguration lang;

    public MessageUtil(YamlFile yamlFile) {
        if(yamlFile != null)
            this.lang = yamlFile.getConfig();
    }

//    public void sendCustomMessage(CommandSender sender, String message) {
//        send(sender, message, null);
//    }
//
//    public void sendCustomMessage(CommandSender sender, String message, HashMap<String, String> data) {
//        send(sender, message, data);
//    }
//
//    public void sendMessage(CommandSender sender, String path) {
//        send(sender, getMessage(path), null);
//    }
//
//    public void sendMessage(CommandSender sender, String path, Map<String, String> data) {
//        send(sender, getMessage(path), data);
//    }

    public void sendList(CommandSender sender, String path) {
        sendList(sender, path, null);
    }

    public void sendList(CommandSender sender, String path, Map<String, String> placeholders) {
        List<String> list = lang.getStringList(path);
        for(String line : list) {
            send(sender, line, placeholders);
        }
    }

    public void sendTitle(Player player, String title, String subtitle, Map<String, String> placeholders,
                          int in, int stay, int out) {
        player.sendTitle(replacePlaceholders(title, placeholders), replacePlaceholders(subtitle, placeholders), in, stay, out);
    }

    private void send(CommandSender sender, String message, Map<String, String> placeholders) {
        if(message == null || message.isEmpty()) return;
        sender.sendMessage(replacePlaceholders(message, placeholders));
    }

    public String replacePlaceholders(String string, Map<String, String> placeholders) {
        if(placeholders == null || placeholders.isEmpty())
            return format(string);

        Matcher match = PLACEHOLDER_PATTERN.matcher(string);
        while(match.find()) {
            String placeholder = string.substring(match.start(), match.end());
            String placeholderInner = string.substring(match.start() + 1, match.end() - 1);
            string = string.replace(placeholder, placeholders.getOrDefault(placeholderInner, ""));
            match = PLACEHOLDER_PATTERN.matcher(string);
        }
        return format(string);
    }

    public static List<String> replacePlaceholders(List<String> list, Map<String, String> placeholders) {
        if(placeholders == null || placeholders.isEmpty())
            return format(list);

        List<String> newList = new ArrayList<>();
        for(String line : list) {
            Matcher match = PLACEHOLDER_PATTERN.matcher(line);
            while (match.find()) {
                String placeholder = line.substring(match.start(), match.end());
                String placeholderInner = line.substring(match.start() + 1, match.end() - 1);
                line = line.replace(placeholder, placeholders.getOrDefault(placeholderInner, ""));
                match = PLACEHOLDER_PATTERN.matcher(line);
            }
            newList.add(line);
        }
        return format(newList);
    }

    public String getMessage(String path) {
        if(lang == null || lang.getString(path) == null)
            return "";

        return lang.getString(path);
    }
}
