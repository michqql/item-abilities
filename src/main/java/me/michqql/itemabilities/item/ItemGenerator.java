package me.michqql.itemabilities.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.michqql.itemabilities.ItemAbilitiesPlugin;
import me.michqql.itemabilities.data.JsonFile;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemGenerator {

    public static ItemStack generateItem(final ItemAbilitiesPlugin plugin, final JsonFile file) {
        JsonObject json = file.getJsonObject();
        if(json == null || json.isJsonNull()) {
            throw new IllegalArgumentException("Could not find json file named " + file.getPath());
        }
        // Contains 3 properties - item_id (string), item (object)
        String id = json.get("item_id").getAsString();

         // Parse file and create ItemStack first with "item" object
        ItemStack itemStack = createBasicItem(json.getAsJsonObject("item"));
        ItemMeta meta = itemStack.getItemMeta();
        if(meta != null) {
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(new NamespacedKey(plugin, "ia-item-id"), PersistentDataType.STRING, id);
            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    private static ItemStack createBasicItem(final JsonObject item) {
        /*
         * 3 properties - name (string), material (string), lore (array of string)
         */
        String name = ChatColor.translateAlternateColorCodes('&', item.get("name").getAsString());
        Material material = Material.getMaterial(item.get("material").getAsString());
        if(material == null) throw new IllegalArgumentException("Material cannot be null");

        JsonArray array = item.getAsJsonArray("lore");
        List<String> lore = new ArrayList<>();
        for(JsonElement element : array) {
            lore.add(ChatColor.translateAlternateColorCodes('&', element.getAsString()));
        }

        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        if(meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }
}
