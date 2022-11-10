package me.michqql.itemabilities.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.michqql.core.io.JsonFile;
import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.Settings;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ItemUUIDTracker {

    private static final ItemAbilityPlugin PLUGIN;
    private static final ConcurrentHashMap<UUID, ConcurrentLinkedQueue<JsonObject>> TRACK_CACHE = new ConcurrentHashMap<>();

    static {
        PLUGIN = ItemAbilityPlugin.getInstance();
    }

    public static void init() {
        final long period = Settings.ITEM_TRACKER_AUTO_SAVE_TIME.getIntValue() * 20L; // convert to ticks
        final int updateLimit = Settings.ITEM_TRACKER_LIMIT.getIntValue();
        new BukkitRunnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                int saves = 0;
                int updates = 0;
                for(Map.Entry<UUID, ConcurrentLinkedQueue<JsonObject>> entry : TRACK_CACHE.entrySet()) {
                    if(updateLimit > 0 && updates >= updateLimit)
                        break;

                    final String uuidStr = entry.getKey().toString();
                    final JsonFile file = new JsonFile(PLUGIN, "tracking", uuidStr);
                    final JsonObject object = getJsonObjectFromFile(file);

                    JsonObject uuidObject;
                    if(object.has(uuidStr)) {
                        uuidObject = object.getAsJsonObject(uuidStr);
                    } else {
                        // Unknown uuid entry
                        registerNewUUID(entry.getKey(), "unknown");
                        if(object.has(uuidStr)) {
                            uuidObject = object.getAsJsonObject(uuidStr);
                        } else {
                            // unable to save this item
                            PLUGIN.getLogger().severe("Unable to save tracks for item " + entry.getKey());
                            // remove from cache
                            TRACK_CACHE.remove(entry.getKey());
                            continue;
                        }
                    }

                    JsonArray trackArray;
                    if(uuidObject.has("tracker")) {
                        trackArray = uuidObject.getAsJsonArray("tracker");
                    } else {
                        trackArray = new JsonArray();
                        uuidObject.add("tracker", trackArray);
                    }

                    for(JsonObject tracker : entry.getValue()) {
                        if(updateLimit > 0 && updates >= updateLimit)
                            break;

                        trackArray.add(tracker);
                        updates++;
                        saves++;
                    }
                    file.save();
                }
                PLUGIN.getLogger().info("Saved " + saves + " item tracker" + (saves != 1 ? "s" : "")
                        + " in " + (System.currentTimeMillis() - start) + "ms");
            }
        }.runTaskTimerAsynchronously(PLUGIN, period, period);
    }

    public static void shutdown() {
        final long start = System.currentTimeMillis();
        int saves = 0;

        for(Map.Entry<UUID, ConcurrentLinkedQueue<JsonObject>> entry : TRACK_CACHE.entrySet()) {
            final String uuidStr = entry.getKey().toString();
            final JsonFile file = new JsonFile(PLUGIN, "tracking", uuidStr);
            final JsonObject object = getJsonObjectFromFile(file);

            JsonObject uuidObject;
            if(object.has(uuidStr)) {
                uuidObject = object.getAsJsonObject(uuidStr);
            } else {
                // Unknown uuid entry
                registerNewUUID(entry.getKey(), "unknown");
                if(object.has(uuidStr)) {
                    uuidObject = object.getAsJsonObject(uuidStr);
                } else {
                    // unable to save this item
                    PLUGIN.getLogger().severe("Unable to save tracks for item " + entry.getKey());
                    // remove from cache
                    TRACK_CACHE.remove(entry.getKey());
                    continue;
                }
            }

            JsonArray trackArray;
            if(uuidObject.has("tracker")) {
                trackArray = uuidObject.getAsJsonArray("tracker");
            } else {
                trackArray = new JsonArray();
                uuidObject.add("tracker", trackArray);
            }

            for(JsonObject tracker : entry.getValue()) {
                trackArray.add(tracker);
                saves++;
            }
            file.save();
        }
        PLUGIN.getLogger().info("Saved " + saves + " item tracker" + (saves != 1 ? "s" : "")
                + " in " + (System.currentTimeMillis() - start) + "ms");
    }

    /**
     * Should only be called by ItemGenerator
     * Signifies that this UUID has been legitimately generated and therefore is a duplicate
     * @param uuid the item uuid
     * @param itemId the type of item
     */
    static void registerNewUUID(UUID uuid, String itemId) {
        final long start = System.currentTimeMillis();
        final String uuidStr = uuid.toString();

        // Save instantly to hard drive
        final JsonFile file = new JsonFile(PLUGIN, "tracking", uuidStr);
        final JsonObject object = getJsonObjectFromFile(file);
        if(object.has(uuidStr))
            throw new IllegalStateException("UUID has already been registered: " + uuidStr);

        JsonObject uuidObject = new JsonObject();
        object.add(uuidStr, uuidObject);
        uuidObject.addProperty("registered_timestamp", System.currentTimeMillis());
        uuidObject.addProperty("uuid_owner", ItemGenerator.TRACKER_UUID.toString());
        uuidObject.addProperty("item_type_id", itemId);

        JsonArray trackArray = new JsonArray();
        uuidObject.add("tracker", trackArray);

        file.save();
        PLUGIN.getLogger().info("Saved 1 new item tracker"
                + " in " + (System.currentTimeMillis() - start) + "ms");
    }

    static boolean isUUIDRegistered(UUID uuid) {
        JsonFile file = new JsonFile(PLUGIN, "tracking", uuid.toString());

        JsonElement element = file.getJsonElement();
        if(!element.isJsonObject())
            return false;

        JsonObject object = element.getAsJsonObject();
        return object.get(uuid.toString()) != null;
    }

    public static class Tracker {

        /**
         * Item was last seen the players inventory
         * @param uuid the item uuid
         * @param inventoryOwner the player whose inventory the item is in
         */
        public static void playerInventory(UUID uuid, Player inventoryOwner) {
            final JsonObject object = new JsonObject();
            object.addProperty("timestamp", System.currentTimeMillis());
            object.addProperty("type", "player_inventory");

            JsonObject context = new JsonObject();
            object.add("context", context);
            context.addProperty("player_uuid", inventoryOwner.getUniqueId().toString());

            TRACK_CACHE.compute(uuid, (u1, queue) -> {
                if(queue == null)
                    queue = new ConcurrentLinkedQueue<>();

                queue.add(object);
                return queue;
            });
        }

        /**
         * Item was last seen in tile inventory
         * @param uuid the item uuid
         * @param block the block
         * @param container the container inventory
         */
        public static void tileInventory(UUID uuid, Block block, Container container) {
            final JsonObject object = new JsonObject();
            object.addProperty("timestamp", System.currentTimeMillis());
            object.addProperty("type", "tile_inventory");

            JsonObject context = new JsonObject();
            object.add("context", context);
            context.addProperty("block_type", block.getType().toString());
            JsonObject location = new JsonObject();
            Location loc = block.getLocation();
            context.add("block_location", location);
            location.addProperty("world", block.getWorld().getName());
            location.addProperty("x", loc.getBlockX());
            location.addProperty("y", loc.getBlockY());
            location.addProperty("z", loc.getBlockZ());

            TRACK_CACHE.compute(uuid, (u1, queue) -> {
                if(queue == null)
                    queue = new ConcurrentLinkedQueue<>();

                queue.add(object);
                return queue;
            });
        }

        /**
         * Item was last seen as a dropped item entity
         * @param uuid the item uuid
         * @param item the entity
         */
        public static void itemEntity(UUID uuid, Item item) {
            final JsonObject object = new JsonObject();
            object.addProperty("timestamp", System.currentTimeMillis());
            object.addProperty("type", "item_entity");

            JsonObject context = new JsonObject();
            object.add("context", context);
            context.addProperty("entity_uuid", item.getUniqueId().toString());
            context.addProperty("entity_id", item.getEntityId());
            context.addProperty("thrower_uuid", item.getThrower() != null ? item.getThrower().toString() : "null");

            JsonObject location = new JsonObject();
            Location loc = item.getLocation();
            context.add("block_location", location);
            location.addProperty("world", item.getWorld().getName());
            location.addProperty("x", loc.getX());
            location.addProperty("y", loc.getY());
            location.addProperty("z", loc.getZ());

            TRACK_CACHE.compute(uuid, (u1, queue) -> {
                if(queue == null)
                    queue = new ConcurrentLinkedQueue<>();

                queue.add(object);
                return queue;
            });
        }
    }

    private static JsonObject getJsonObjectFromFile(JsonFile file) {
        JsonElement element = file.getJsonElement();
        JsonObject object;
        if(!element.isJsonObject()) {
            object = new JsonObject();
            file.setElement(object);
        } else {
            object = element.getAsJsonObject();
        }
        return object;
    }
}