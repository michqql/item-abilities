package me.michqql.itemabilities.data;

import jdk.nashorn.internal.runtime.logging.DebugLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class CommentFile extends YamlFile {

    public CommentFile(Plugin plugin, Path path) {
        super(plugin, path);
    }

    @Override
    protected void copy(Plugin plugin) {
        InputStream in = plugin.getResource(path.getPath());
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
    public void save() { /* DO NOTHING WHEN SAVING */ }
}
