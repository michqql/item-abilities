package me.michqql.itemabilities.item;

import me.michqql.itemabilities.ItemAbilitiesPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemModifier {

    public static String getIdOnItem(final ItemAbilitiesPlugin plugin, final ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if(meta == null)
            return "";

        PersistentDataContainer data = meta.getPersistentDataContainer();
        final NamespacedKey key = new NamespacedKey(plugin, "ia-item-id");
        if(!data.has(key, PersistentDataType.STRING))
            return "";

        return data.get(key, PersistentDataType.STRING);
    }

    public static boolean isItemOfType(final ItemAbilitiesPlugin plugin, final ItemStack itemStack, final String itemId) {
        return itemId.equals(getIdOnItem(plugin, itemStack));
    }

    public static String getItemName(final ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null && meta.hasDisplayName() ? meta.getDisplayName() : itemStack.getType().toString();
    }

    public static void setPropertyValue(final ItemAbilitiesPlugin plugin, final ItemStack itemStack, final String tag, final Integer value) {
        ItemMeta meta = itemStack.getItemMeta();
        if(meta == null)
            return;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        final NamespacedKey key = new NamespacedKey(plugin, "iap-" + tag);
        data.set(key, PersistentDataType.INTEGER, value);
        itemStack.setItemMeta(meta);
    }

    public static Integer getPropertyValue(final ItemAbilitiesPlugin plugin, final ItemStack itemStack, final String tag) {
        ItemMeta meta = itemStack.getItemMeta();
        if(meta == null)
            return -1;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        final NamespacedKey key = new NamespacedKey(plugin, "iap-" + tag);
        if(!data.has(key, PersistentDataType.INTEGER))
            return -1;

        return data.get(key, PersistentDataType.INTEGER);
    }

    public static Integer getAndSetPropertyValue(final ItemAbilitiesPlugin plugin, final ItemStack itemStack,
                                              final String tag, final GetPropertyResponse response) {

        if(response == null)
            return -1;

        ItemMeta meta = itemStack.getItemMeta();
        if(meta == null)
            return -1;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        final NamespacedKey key = new NamespacedKey(plugin, "iap-" + tag);
        if(!data.has(key, PersistentDataType.INTEGER))
            return -1;

        Integer value = data.get(key, PersistentDataType.INTEGER);
        Integer responseValue = response.handle(value);
        data.set(key, PersistentDataType.INTEGER, responseValue);
        itemStack.setItemMeta(meta);
        return responseValue;
    }

    public interface GetPropertyResponse {
        Integer handle(Integer value);
    }
}
