package me.michqql.itemabilities;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import java.util.HashMap;
import java.util.logging.Level;

public class ItemAbilityHandler {

    private static final HashMap<String, ItemAbility> ITEM_ABILITIES = new HashMap<>();

    static void registerAbility(ItemAbility ability) {
        HandlerList.unregisterAll(ability);
        ITEM_ABILITIES.put(ability.getIdentifier(), ability);
        Bukkit.getPluginManager().registerEvents(ability, ability.plugin);
        Bukkit.getLogger().log(Level.INFO, "Registered item ability: " + ability.getIdentifier());
    }

    public static ItemAbility getAbility(String id) {
        return ITEM_ABILITIES.get(id);
    }
}
