package me.michqql.itemabilities.enchant.enchants;

import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.enchant.EnchantWrapper;
import me.michqql.itemabilities.event.AbilityBreakBlockEvent;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;

public class TelekenesisEnchant extends EnchantWrapper {

    public TelekenesisEnchant(ItemAbilityPlugin plugin) {
        super(plugin, "telekenesis");
    }

    // Must listen on high priority to let other components use this event first
    // If event is cancelled, should not give player the blocks
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        if(e.isCancelled() || !isPlayerHoldingCorrectItem(e.getPlayer(), true)) // Tools can only be used in main hand
            return;

        e.setDropItems(false); // Stop the block drops

        giveItems(e.getPlayer(), e.getBlock());
    }

    @EventHandler
    public void onAbilityBlockBreak(AbilityBreakBlockEvent e) {
        if(!isPlayerHoldingCorrectItem(e.getPlayer(), true)) // Tools can only be used in main hand
            return;

        e.setBlockDrop(false);

        giveItems(e.getPlayer(), e.getBlock());
    }

    private void giveItems(Player player, Block block) {
        Collection<ItemStack> drops = block.getDrops(); // Get the items that the block would have dropped

        // Try to add the items to the players inventory
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(drops.toArray(new ItemStack[0]));
        player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f);

        // Drop all items that didn't fit into the world naturally
        if(!overflow.isEmpty()) {
            World world = player.getWorld();
            overflow.values().forEach(itemStack -> world.dropItemNaturally(player.getLocation(), itemStack));
        }
    }
}
