package me.michqql.itemabilities.enchant;

import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.item.ItemGenerator;
import me.michqql.itemabilities.item.NKeyCache;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.HandlerList;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;

public class EnchantHandler {

    private static final HashMap<NamespacedKey, EnchantWrapper> ENCHANTMENTS = new HashMap<>();

    static void registerEnchantment(EnchantWrapper enchant) {
        HandlerList.unregisterAll(enchant);
        boolean success = registerEnchantmentAccept(enchant);
        if(success) {
            ENCHANTMENTS.put(enchant.getKey(), enchant);
            Bukkit.getPluginManager().registerEvents(enchant, enchant.plugin);
            ItemAbilityPlugin.getLog().info("Registered enchantment " + enchant.enchantId);
        } else {
            ItemAbilityPlugin.getLog().warning("Unable to register enchantment " + enchant.enchantId);
        }
    }

    public static EnchantWrapper getEnchant(String id) {
        return ENCHANTMENTS.get(NKeyCache.getNKey(ItemGenerator.ENCHANTMENT_ID_KEY + id));
    }

    public static Collection<EnchantWrapper> getEnchants() {
        return ENCHANTMENTS.values();
    }

    private static boolean registerEnchantmentAccept(Enchantment enchantment) {
        try {
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);
            field.set(null, true);
            Enchantment.registerEnchantment(enchantment);
            return true;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }
}
