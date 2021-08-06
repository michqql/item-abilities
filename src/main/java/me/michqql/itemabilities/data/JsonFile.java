package me.michqql.itemabilities.data;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.logging.Level;

public class JsonFile extends DataFile {

    private final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private JsonElement element;

    public JsonFile(Plugin plugin, String path) {
        super(plugin, path.endsWith(".json") ? path : path + ".json");
    }

    @Override
    protected void copy(Plugin plugin) {
        InputStream in = plugin.getResource(path);
        if(in == null) {
            Bukkit.getLogger().log(Level.WARNING, "Could not find resource to copy named " + path);
            return;
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];

            int length;
            while((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            out.close();
            in.close();
        } catch(IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not copy default resource named " + path);
        }
    }

    @Override
    protected void init() {
        try {
            this.element = JsonParser.parseReader(new FileReader(this.file));
        } catch (FileNotFoundException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not read json file named " + path);
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        try {
            FileWriter fw = new FileWriter(this.file);
            fw.write(GSON.toJson(element));
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not save json file named " + path);
            e.printStackTrace();
        }
    }

    public JsonObject getJsonObject() {
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }
}
