package me.michqql.itemabilities.util;

import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ChatColor;

public class ChatColorUtil {

    public static ChatColor of(String string) {
        Preconditions.checkArgument( string != null, "string cannot be null" );
        if (string.startsWith("#") && string.length() == 7) {
            return ChatColor.of(string);
        }

        char c = string.charAt(string.length() - 1); // get the last char in the string
        return ChatColor.getByChar(c);
    }
}
