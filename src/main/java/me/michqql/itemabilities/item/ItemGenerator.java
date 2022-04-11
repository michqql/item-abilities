package me.michqql.itemabilities.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.michqql.core.io.JsonFile;
import me.michqql.core.util.MessageHandler;
import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.item.data.NumberDataType;
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

    private final static ItemAbilityPlugin PLUGIN = ItemAbilityPlugin.getInstance();
    public final static String ITEM_ID_KEY = "item-id";
    public final static String PROPERTY_ID_KEY = "property-";

    public static ItemStack generateItem(final String itemId) {
        return generateItem(new JsonFile(PLUGIN, "items", itemId));
    }

    public static ItemStack generateItem(final JsonFile file) {
        JsonElement element = file.getJsonElement();
        if(!element.isJsonObject()) {
            throw new IllegalArgumentException("Could not read json file");
        }

        JsonObject json = element.getAsJsonObject();

        // Contains 3 properties - item_id (string), properties (array of objects), item (object)
        String id = json.get("item_id").getAsString();
        JsonArray propertyArray = json.getAsJsonArray("properties");
        JsonObject item = json.getAsJsonObject("item");

        // Parse file and create ItemStack first with "item" object
        Tuple<Material, String, List<String>> itemData = readItemObject(item);
        ItemStack itemStack = new ItemStack(itemData.a);

        ItemMeta meta = itemStack.getItemMeta();
        if(meta != null) {
            PersistentDataContainer data = meta.getPersistentDataContainer();

            // Item ID
            data.set(NKeyCache.getNKey(ITEM_ID_KEY), PersistentDataType.STRING, id);

            // Properties
            HashMap<String, String> propertyPlaceholders = new HashMap<>();
            for(JsonElement arrayElement : propertyArray) {
                if(!arrayElement.isJsonObject())
                    continue;

                JsonObject object = arrayElement.getAsJsonObject();
                String tag = object.get("tag").getAsString();
                Number value = object.get("value").getAsNumber();

                propertyPlaceholders.put(tag, String.valueOf(value.intValue()));
                data.set(NKeyCache.getNKey(PROPERTY_ID_KEY + tag), NumberDataType.NUMBER, value);
            }

            meta.setDisplayName(MessageHandler.replacePlaceholdersStatic(itemData.b, propertyPlaceholders));
            meta.setLore(MessageHandler.replacePlaceholdersStatic(itemData.c, propertyPlaceholders));
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    public static void updateItem(final ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if(meta != null) {
            PersistentDataContainer data = meta.getPersistentDataContainer();

            // Properties
            HashMap<String, String> propertyPlaceholders = new HashMap<>();
            for(NamespacedKey nkey : data.getKeys()) {
                String key = nkey.getKey();
                if(!key.startsWith(PROPERTY_ID_KEY) || key.length() <= PROPERTY_ID_KEY.length())
                    continue;

                String tag = key.substring(9);
                System.out.println("Updating property: " + tag);

                Number value = data.get(nkey, NumberDataType.NUMBER);
                if(value == null)
                    continue;

                System.out.println("Current value: " + value.intValue());
                propertyPlaceholders.put(tag, String.valueOf(value.intValue()));
            }
            itemStack.setItemMeta(meta);
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














