package me.michqql.itemabilities.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.logging.Level;

public class YamlFile extends DataFile {

    private FileConfiguration config;

    public YamlFile(Plugin plugin, Path path) {
        super(plugin, path.setExtension("yml"));
    }

    public FileConfiguration getConfig() {
        return config;
    }

    @Override
    protected void init() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Could not save yaml file named " + path);
            e.printStackTrace();
        }
    }
}
