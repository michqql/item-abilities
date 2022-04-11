package me.michqql.itemabilities.item;

import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.item.data.NumberDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ItemModifier {

    private final static ItemAbilityPlugin PLUGIN = ItemAbilityPlugin.getInstance();

    public static String getIdOnItem(final ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if(meta == null)
            return "";

        PersistentDataContainer data = meta.getPersistentDataContainer();
        final NamespacedKey key = NKeyCache.getNKey(ItemGenerator.ITEM_ID_KEY);
        if(!data.has(key, PersistentDataType.STRING))
            return "";

        return data.get(key, PersistentDataType.STRING);
    }

    public static boolean isItemOfType(final ItemStack itemStack, final String itemId) {
        return itemId.equals(getIdOnItem(itemStack));
    }

    public static String getItemName(final ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null && meta.hasDisplayName() ? meta.getDisplayName() : itemStack.getType().toString();
    }

    public static void setPropertyValue(final ItemStack itemStack, final String tag, final Number value) {
        ItemMeta meta = itemStack.getItemMeta();
        if(meta == null)
            return;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        final NamespacedKey key = NKeyCache.getNKey(ItemGenerator.PROPERTY_ID_KEY + tag);
        data.set(key, NumberDataType.NUMBER, value);
        itemStack.setItemMeta(meta);
    }

    public static Number getPropertyValue(final ItemStack itemStack, final String tag) {
        ItemMeta meta = itemStack.getItemMeta();
        if(meta == null)
            return -1;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        final NamespacedKey key = NKeyCache.getNKey(ItemGenerator.PROPERTY_ID_KEY + tag);
        if(!data.has(key, NumberDataType.NUMBER))
            return -1;

        return data.get(key, NumberDataType.NUMBER);
    }

    public static Number getAndSetPropertyValue(final ItemStack itemStack, final String tag,
                                                 @NotNull Function<Number, Number> function) {

        ItemMeta meta = itemStack.getItemMeta();
        if(meta == null)
            return null;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        final NamespacedKey key = NKeyCache.getNKey(ItemGenerator.PROPERTY_ID_KEY + tag);
        if(!data.has(key, NumberDataType.NUMBER))
            return null;

        Number value = data.get(key, NumberDataType.NUMBER);
        value = function.apply(value);
        if(value == null)
            return null;

        data.set(key, NumberDataType.NUMBER, value);
        itemStack.setItemMeta(meta);
        return value;
    }
}
