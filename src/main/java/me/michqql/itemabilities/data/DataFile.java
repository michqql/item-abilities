package me.michqql.itemabilities.data;

import jdk.nashorn.internal.runtime.logging.DebugLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public abstract class DataFile {

    private final File dataFolder;
    protected File directory, file;
    protected final String path;

    public DataFile(Plugin plugin, String path) {
        this.dataFolder = plugin.getDataFolder();
        this.path = path;

        preInit(plugin);
        init();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void preInit(Plugin plugin) {
        if(!this.dataFolder.exists())
            this.dataFolder.mkdirs();

        this.file = new File(dataFolder, path);

        if(!this.file.exists()) {
            try {
                this.file.createNewFile();
                copy(plugin);
            } catch(IOException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Could not create file " + path + "!");
                Bukkit.getLogger().log(Level.SEVERE, "Exact file path: " + file.getAbsolutePath());
                e.printStackTrace();
            }
        }
    }

    protected void copy(Plugin plugin) {}
    protected abstract void init();
    public abstract void save();

    public String getPath() {
        return path;
    }
}
