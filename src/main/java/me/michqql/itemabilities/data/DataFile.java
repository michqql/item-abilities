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
    protected final Path path;

    public DataFile(Plugin plugin, Path path) {
        this.dataFolder = plugin.getDataFolder();
        this.path = path;

        preInit(plugin);
        init();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void preInit(Plugin plugin) {
        if(!this.dataFolder.exists())
            this.dataFolder.mkdirs();

        if(path.isUsingDirectory()) {
            this.directory = new File(dataFolder, path.getDirectoryName());
            if(!directory.exists())
                directory.mkdirs();

            this.file = new File(directory, path.getFileName());
        } else {
            this.file = new File(dataFolder, path.getFileName());
        }

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

    public Path getPath() {
        return path;
    }

    public static class Path {
        final String directoryName, fileName;
        String extension;
        final boolean usesDirectory;

        public Path(String directoryName, String fileName, String extension) {
            this.directoryName = directoryName;
            this.fileName = fileName;
            this.extension = extension;

            this.usesDirectory = directoryName != null && !directoryName.isEmpty();
        }

        public String getDirectoryName() {
            return directoryName;
        }

        public boolean isUsingDirectory() {
            return usesDirectory;
        }

        public String getSimpleFileName() {
            return fileName;
        }

        public String getFileName() {
            return fileName + "." + extension;
        }

        public String getExtension() {
            return extension;
        }

        public String getPath() {
            return (usesDirectory ? directoryName + "/" : "") + fileName + "." + extension;
        }

        public Path setExtension(String extension) {
            this.extension = extension;
            return this;
        }
    }
}
