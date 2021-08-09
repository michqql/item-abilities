package me.michqql.itemabilities.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.michqql.itemabilities.ItemAbilitiesPlugin;
import me.michqql.itemabilities.data.DataFile;
import me.michqql.itemabilities.data.JsonFile;
import me.michqql.itemabilities.util.MessageUtil;
import me.michqql.itemabilities.util.Tuple;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemGenerator {

    public static ItemStack generateItem(final ItemAbilitiesPlugin plugin, final String itemId) {
        return generateItem(plugin, new JsonFile(plugin, new DataFile.Path("items", itemId, "json")));
    }

    public static ItemStack generateItem(final ItemAbilitiesPlugin plugin, final JsonFile file) {
        JsonObject json = file.getJsonObject();
        if(json == null || json.isJsonNull()) {
            throw new IllegalArgumentException("Could not find json file named " + file.getPath());
        }
        // Contains 3 properties - item_id (string), properties (array of objects), item (object)
        String id = json.get("item_id").getAsString();
        JsonArray properties = json.getAsJsonArray("properties");
        JsonObject item = json.getAsJsonObject("item");

        // Parse file and create ItemStack first with "item" object
        Tuple<Material, String, List<String>> itemData = readItemObject(item);
        ItemStack itemStack = new ItemStack(itemData.a);
        ItemMeta meta = itemStack.getItemMeta();
        if(meta != null) {
            PersistentDataContainer data = meta.getPersistentDataContainer();
            // Id
            data.set(new NamespacedKey(plugin, "ia-item-id"), PersistentDataType.STRING, id);

            // Props
            HashMap<String, String> propertyPlaceholders = new HashMap<>();
            for(JsonElement element : properties) {
                if(!element.isJsonObject())
                    continue;

                JsonObject object = element.getAsJsonObject();
                String tag = object.get("tag").getAsString();
                int value = object.get("value").getAsInt();

                propertyPlaceholders.put(tag, String.valueOf(value));
                data.set(new NamespacedKey(plugin, "iap-" + tag), PersistentDataType.INTEGER, value);
            }

            meta.setDisplayName(MessageUtil.format(itemData.b));
            meta.setLore(MessageUtil.replacePlaceholders(itemData.c, propertyPlaceholders));
            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    public static void updateItem(final ItemAbilitiesPlugin plugin, final ItemStack toUpdate) {
        updateItem(
                new JsonFile(
                        plugin,
                        new DataFile.Path("items", ItemModifier.getIdOnItem(plugin, toUpdate), "json")
                ),
                toUpdate
        );
    }

    public static void updateItem(final JsonFile file, final ItemStack toUpdate) {
        JsonObject json = file.getJsonObject();
        if(json == null || json.isJsonNull()) {
            throw new IllegalArgumentException("Could not find json file named " + file.getPath());
        }

        JsonObject item = json.getAsJsonObject("item");
        Tuple<Material, String, List<String>> itemData = readItemObject(item);

        ItemMeta meta = toUpdate.getItemMeta();
        if(meta != null) {
            PersistentDataContainer data = meta.getPersistentDataContainer();

            // Props
            HashMap<String, String> propertyPlaceholders = new HashMap<>();
            for(NamespacedKey key : data.getKeys()) {
                if(!key.getKey().startsWith("iap-"))
                    continue;

                String tag = key.getKey().substring(4);
                Integer value = data.get(key, PersistentDataType.INTEGER);

                propertyPlaceholders.put(tag, String.valueOf(value));
            }

            meta.setDisplayName(MessageUtil.format(itemData.b));
            meta.setLore(MessageUtil.replacePlaceholders(itemData.c, propertyPlaceholders));
            toUpdate.setItemMeta(meta);
        }
    }

    private static Tuple<Material, String, List<String>> readItemObject(final JsonObject object) {
        Tuple<Material, String, List<String>> tuple = new Tuple<>();

        /*
         * 3 properties - name (string), material (string), lore (array of string)
         */

        // Material
        Material material = Material.getMaterial(object.get("material").getAsString());
        if(material == null) throw new IllegalArgumentException("Material provided is invalid/null");

        // Name
        String name = object.get("name").getAsString();

        // Lore
        JsonArray array = object.getAsJsonArray("lore");
        List<String> lore = new ArrayList<>();
        for(JsonElement element : array) {
            lore.add(ChatColor.translateAlternateColorCodes('&', element.getAsString()));
        }

        tuple.a = material;
        tuple.b = name;
        tuple.c = lore;
        return tuple;
    }


}
