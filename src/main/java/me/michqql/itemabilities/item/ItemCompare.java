package me.michqql.itemabilities.item;

import me.michqql.itemabilities.ItemAbilitiesPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemCompare {

    public static boolean isItemOfType(final ItemAbilitiesPlugin plugin, final ItemStack itemStack, final String itemId) {
        ItemMeta meta = itemStack.getItemMeta();
        if(meta == null)
            return false;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        final NamespacedKey key = new NamespacedKey(plugin, "ia-item-id");
        if(!data.has(key, PersistentDataType.STRING))
            return false;

        final String idOnItem = data.get(key, PersistentDataType.STRING);
        return itemId.equals(idOnItem);
    }

    public static String getItemName(final ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null && meta.hasDisplayName() ? meta.getDisplayName() : itemStack.getType().toString();
    }
}
