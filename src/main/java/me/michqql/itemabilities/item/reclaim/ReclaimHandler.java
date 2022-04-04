package me.michqql.itemabilities.item.reclaim;

import me.michqql.core.io.YamlFile;
import me.michqql.itemabilities.ItemAbilitiesPlugin;
import me.michqql.itemabilities.item.ItemGenerator;
import me.michqql.itemabilities.util.Pair;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ReclaimHandler {

    private final ItemAbilitiesPlugin plugin;

    public ReclaimHandler(ItemAbilitiesPlugin plugin) {
        this.plugin = plugin;
    }

    public void giveItem(UUID uuid, String itemId, int amount) {
            YamlFile yamlFile = new YamlFile(plugin, "reclaim", uuid.toString());
            FileConfiguration f = yamlFile.getConfig();
            int savedAmount = f.getInt(itemId, 0);
            yamlFile.getConfig().set(itemId, amount + savedAmount);
            yamlFile.save();
    }

    public Pair<String, Integer> getNextItem(UUID uuid) {
        YamlFile yamlFile = new YamlFile(plugin, "reclaim", uuid.toString());
        FileConfiguration f = yamlFile.getConfig();
        for(String itemId : f.getKeys(false)) {
            int amount = f.getInt(itemId, 0);
            if (amount > 0) {
                f.set(itemId, null);
                yamlFile.save();
                return new Pair<>(itemId, amount);
            }
        }
        return null;
    }

    /**
     * @return integer array
     * a[0] = reclaimed item amount
     * a[1] = remaining item amount
     */
    public int[] reclaimItems(Player player) {
        YamlFile yamlFile = new YamlFile(plugin, "reclaim", player.getUniqueId().toString());
        FileConfiguration f = yamlFile.getConfig();
        Set<String> itemIds = f.getKeys(false);
        Iterator<String> iterator = itemIds.iterator();

        Inventory inv = player.getInventory();
        int reclaimed = 0;

        while(inv.firstEmpty() != -1 && iterator.hasNext()) {
            String itemId = iterator.next();
            int amount = f.getInt(itemId, 0);
            if(amount > 0) {
                f.set(itemId, null);
                ItemStack item = ItemGenerator.generateItem(plugin, itemId);
                item.setAmount(amount);
                inv.addItem(item);
                reclaimed++;
            }
        }

        yamlFile.save();
        return new int[] { reclaimed, f.getKeys(false).size() };
    }
}
