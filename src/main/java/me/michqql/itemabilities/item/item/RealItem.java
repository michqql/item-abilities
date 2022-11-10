package me.michqql.itemabilities.item.item;

import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.item.ItemGenerator;
import me.michqql.itemabilities.item.NKeyCache;
import me.michqql.itemabilities.item.data.BooleanDataType;
import me.michqql.itemabilities.item.data.NumberDataType;
import me.michqql.itemabilities.item.data.UUIDDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RealItem {

    private final ItemStack itemStack;
    private final ItemMeta meta;

    // Persistent Data
    private String itemId;
    private UUID itemUUID;
    private boolean example;
    private List<Property> properties;
    private List<String> enchants;
    private boolean enchantable;
    private boolean reforgable;

    public RealItem(ItemStack item) {
        this.itemStack = item;
        this.meta = item.getItemMeta();

        getDataFromItem();
    }

    private void getDataFromItem() {
        if(meta != null) {
            PersistentDataContainer data = meta.getPersistentDataContainer();

            // Item ID and UUID
            this.itemId = data.get(NKeyCache.getNKey(ItemGenerator.ITEM_ID_KEY), PersistentDataType.STRING);
            this.itemUUID = data.get(NKeyCache.getNKey(ItemGenerator.TRACKER_ID_KEY), UUIDDataType.UUID);

            // Properties
            final String pluginName = ItemAbilityPlugin.getPluginName();
            this.properties = new ArrayList<>();
            this.enchants = new ArrayList<>();
            for (NamespacedKey nkey : data.getKeys()) {
                // Is this key our key
                if(!nkey.getNamespace().equalsIgnoreCase(pluginName))
                    continue;

                // This key is ours!
                String key = nkey.getKey();

                // Is this item an example? No updates to example items
                if(key.equalsIgnoreCase(ItemGenerator.EXAMPLE_KEY)) {
                    this.example = true;
                    continue;
                }

                // Check if this key is a property
                if(key.startsWith(ItemGenerator.PROPERTY_ID_KEY) &&
                        key.length() > ItemGenerator.PROPERTY_ID_KEY.length()) {
                    String tag = key.substring(ItemGenerator.PROPERTY_ID_KEY.length());
                    Number value = data.get(nkey, NumberDataType.NUMBER);
                    if (value == null)
                        continue;

                    properties.add(new Property(tag, value));
                }

                // Check if this key is an enchantment
                if(key.startsWith(ItemGenerator.ENCHANTMENT_ID_KEY) &&
                        key.length() > ItemGenerator.ENCHANTMENT_ID_KEY.length()) {
                    String id = key.substring(ItemGenerator.ENCHANTMENT_ID_KEY.length());
                    enchants.add(id);
                }
            }

            // Enchantable and reforgable
            this.enchantable = Boolean.TRUE.equals(data.get(NKeyCache.getNKey(ItemGenerator.ENCHANTABLE_KEY), BooleanDataType.BOOLEAN));
            this.reforgable = Boolean.TRUE.equals(data.get(NKeyCache.getNKey(ItemGenerator.REFORGABLE_KEY), BooleanDataType.BOOLEAN));
        }
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemMeta getItemMeta() {
        return meta;
    }

    public String getItemId() {
        return itemId;
    }

    public boolean isItemOfType(String itemId) {
        return getItemId().equalsIgnoreCase(itemId);
    }

    public UUID getItemUniqueId() {
        return itemUUID;
    }

    public boolean isExampleItem() {
        return example;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public boolean isEnchantable() {
        return enchantable;
    }

    public List<String> getCustomEnchants() {
        return enchants;
    }

    public boolean isReforgable() {
        return reforgable;
    }

    public String getDisplayName() {
        return meta != null && meta.hasDisplayName() ? meta.getDisplayName() : itemStack.getType().toString();
    }
}
