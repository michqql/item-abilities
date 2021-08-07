package me.michqql.itemabilities.util;

import com.google.gson.JsonObject;
import me.michqql.itemabilities.data.JsonFile;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;

public class NameToUUIDConverter {

    private static final String REQUEST_UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s";

    public static UUID getUUIDFromName(String playerName) {
        String url = String.format(REQUEST_UUID_URL, playerName);
        String json = readURL(url);
        if(json.isEmpty())
            return null;

        try {
            JsonObject object = JsonFile.PARSER.parse(json).getAsJsonObject();
            String uuid = object.get("id").getAsString();
            return UUID.fromString(formatUUID(uuid));
        } catch(IllegalStateException e) {
            Bukkit.getLogger().log(Level.WARNING, "Error while reading json for UUID");
            e.printStackTrace();
        }
        return null;
    }

    private static String readURL(String urlIn) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL url = new URL(urlIn);

            // Read text returned by server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
            }
            in.close();

        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Error while reading web URL");
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private static String formatUUID(String flat) {
        return flat.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5"
        );
    }
}
