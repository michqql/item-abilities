package me.michqql.itemabilities.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.michqql.core.io.JsonFile;
import me.michqql.core.util.MessageHandler;
import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.item.data.BooleanDataType;
import me.michqql.itemabilities.item.data.NumberDataType;
import me.michqql.itemabilities.item.data.UUIDDataType;
import me.michqql.itemabilities.util.Tuple;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ItemGenerator {

    private final static ItemAbilityPlugin PLUGIN = ItemAbilityPlugin.getInstance();

    // Namespace Key ID's
    public final static String TRACKER_ID_KEY = "tracker-uuid";
    public final static String ITEM_ID_KEY = "item-id";
    public final static String PROPERTY_ID_KEY = "property-";
    public final static String EXAMPLE_KEY = "example";
    public final static String ENCHANTABLE_KEY = "enchantable";
    public final static String ENCHANTMENT_ID_KEY = "enchant-";
    public final static String REFORGABLE_KEY = "enchantable";

    // Json keys
    private final static String ITEM_ID_JSON_KEY = "item_id";
    private final static String PROPERTY_JSON_KEY = "properties";
    private final static String PROPERTY_TAG_JSON_KEY = "tag";
    private final static String PROPERTY_VALUE_JSON_KEY = "value";
    private final static String ENCHANTABLE_JSON_KEY = "enchantable";
    private final static String REFORGABLE_JSON_KEY = "reforgable";
    private final static String ITEM_JSON_KEY = "item";
    private final static String ITEM_FLAG_JSON_KEY = "item_flags";

    protected static UUID TRACKER_UUID = UUID.randomUUID();

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
        final String id = json.get(ITEM_ID_JSON_KEY).getAsString();
        JsonArray propertyArray = json.getAsJsonArray(PROPERTY_JSON_KEY);
        JsonObject item = json.getAsJsonObject(ITEM_JSON_KEY);

        // Parse file and create ItemStack first with "item" object
        Tuple<Material, String, List<String>> itemData = readItemObject(item);
        ItemStack itemStack = new ItemStack(itemData.a);

        ItemMeta meta = itemStack.getItemMeta();
        if(meta != null) {
            PersistentDataContainer data = meta.getPersistentDataContainer();

            // UUID
            UUID uuid = new UUID(UUID.randomUUID().getMostSignificantBits(), TRACKER_UUID.getLeastSignificantBits());
            while(ItemUUIDTracker.isUUIDRegistered(uuid)) { // ensure uuid is not a duplicate
                uuid = new UUID(UUID.randomUUID().getMostSignificantBits(), TRACKER_UUID.getLeastSignificantBits());
            }
            data.set(NKeyCache.getNKey(TRACKER_ID_KEY), UUIDDataType.UUID, uuid);
            ItemUUIDTracker.registerNewUUID(uuid, id);

            // Item ID
            data.set(NKeyCache.getNKey(ITEM_ID_KEY), PersistentDataType.STRING, id);

            // Properties
            for(JsonElement arrayElement : propertyArray) {
                if(!arrayElement.isJsonObject())
                    continue;

                JsonObject object = arrayElement.getAsJsonObject();
                String tag = object.get(PROPERTY_TAG_JSON_KEY).getAsString();
                Number value = object.get(PROPERTY_VALUE_JSON_KEY).getAsNumber();

                data.set(NKeyCache.getNKey(PROPERTY_ID_KEY + tag), NumberDataType.NUMBER, value);
            }

            meta.setDisplayName(MessageHandler.colour(itemData.b));
            meta.setLore(MessageHandler.colour(itemData.c));

            // Enchants and reforges
            if(json.has(ENCHANTABLE_JSON_KEY) && json.get(ENCHANTABLE_JSON_KEY).getAsBoolean()) {
                data.set(NKeyCache.getNKey(ENCHANTABLE_KEY), BooleanDataType.BOOLEAN, true);
            }

            if(json.has(REFORGABLE_JSON_KEY) && json.get(REFORGABLE_JSON_KEY).getAsBoolean()) {
                data.set(NKeyCache.getNKey(REFORGABLE_KEY), BooleanDataType.BOOLEAN, true);
            }

            // Item flags
            if(item.has(ITEM_FLAG_JSON_KEY)) {
                JsonArray flagArray = item.get(ITEM_FLAG_JSON_KEY).getAsJsonArray();
                for (JsonElement flagElement : flagArray) {
                    if (!flagElement.isJsonPrimitive())
                        continue;

                    String string = flagElement.getAsString();
                    ItemFlag flag;
                    try {
                        flag = ItemFlag.valueOf(string);
                        meta.addItemFlags(flag);
                    } catch (IllegalArgumentException ignore) {
                    }
                }
            }

            itemStack.setItemMeta(meta);
        }
        return itemStack;
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

    /**
     * Used for security purposes to track the owner of an item
     * If set to null, a random UUID will be used.
     * @param uuid the tracker uuid
     */
    public static void setTrackerUUID(UUID uuid) {
        TRACKER_UUID = Objects.requireNonNullElseGet(uuid, UUID::randomUUID);
    }
}