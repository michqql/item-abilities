package me.michqql.itemabilities.item;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.michqql.itemabilities.ItemAbilityPlugin;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class NKeyCache {

    private final static ItemAbilityPlugin PLUGIN = ItemAbilityPlugin.getInstance();
    private final static LoadingCache<String, NamespacedKey> CACHE = CacheBuilder.newBuilder()
            .maximumSize(1024)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull NamespacedKey load(@NotNull String key) {
                    return new NamespacedKey(PLUGIN, key);
                }
            });

    public static NamespacedKey getNKey(String key) {
        return CACHE.getUnchecked(key);
    }
}
