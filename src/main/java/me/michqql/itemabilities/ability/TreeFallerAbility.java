package me.michqql.itemabilities.ability;

import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.ItemAbility;
import me.michqql.itemabilities.event.AbilityBreakBlockEvent;
import me.michqql.itemabilities.item.ItemModifier;
import me.michqql.itemabilities.item.item.RealItem;
import me.michqql.itemabilities.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TreeFallerAbility extends ItemAbility {

    private final int MAX_BLOCK_UPDATES_PER_TICK = 4;
    private final static int MAX_BLOCK_UPDATES = 30;
    private final static List<Material> WOOD_BLOCKS = Arrays.asList(
            Material.ACACIA_LOG,
            Material.STRIPPED_ACACIA_LOG,
            Material.ACACIA_WOOD,
            Material.BIRCH_LOG,
            Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG,
            Material.OAK_LOG,
            Material.SPRUCE_LOG
    );

    public TreeFallerAbility(ItemAbilityPlugin plugin) {
        super(plugin, "tree_faller");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        final Player player = e.getPlayer();

        // Player must be holding correct item in hand
        Pair<Boolean, ItemStack> item = isPlayerHoldingCorrectItem(player, false);
        if(!item.key)
            return;

        final Block block = e.getBlock();
        if(!isLogBlock(block)) {
            return;
        }

        // Cancelling the event lets other components know not to interact with this
        // E.g., telekenesis enchantment should not listen to block break event,
        //       as this would cause duplication of item
        e.setCancelled(true);

        final RealItem realItem = ItemModifier.getItemData(item.value);
        ItemModifier.getAndSetPropertyValue(item.value, "uses", (value) -> {
            int remaining = value.intValue() - 1;
            if(remaining <= 0) {
                // break item
                return remaining;
            }
            return remaining;
        });

        new BukkitRunnable() {
            final Queue<Block> toVisit = new LinkedList<>(List.of(block));
            int tickUpdates;
            int totalBlockUpdates;
            @Override
            public void run() {
                tickUpdates = 0;

                while(toVisit.size() > 0 && tickUpdates < MAX_BLOCK_UPDATES_PER_TICK
                        && totalBlockUpdates < MAX_BLOCK_UPDATES) {
                    tickUpdates++;
                    totalBlockUpdates++;

                    Block current = toVisit.poll();
                    if(current == null)
                        break;

                    AbilityBreakBlockEvent result = callAbilityBreakBlockEvent(player, realItem, current);
                    if(result.isBlockDrop()) {
                        // This line can cause recursion problems as this will invoke BlockBreakEvent
                        // if calling player.breakBlock(current)
                        current.breakNaturally();
                    } else {
                        current.setType(Material.AIR);
                    }

                    List<Block> adjacent = getAdjacentBlocks(current);
                    for(Block other : adjacent) {
                        if(isLogBlock(other)) {
                            toVisit.add(other);
                        }
                    }
                }

                if(toVisit.isEmpty() || totalBlockUpdates >= MAX_BLOCK_UPDATES)
                    this.cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private boolean isLogBlock(Block block) {
        return WOOD_BLOCKS.contains(block.getType());
    }

    private List<Block> getAdjacentBlocks(Block block) {
        return List.of(
                block.getRelative(BlockFace.UP),
                block.getRelative(BlockFace.DOWN),
                block.getRelative(BlockFace.NORTH),
                block.getRelative(BlockFace.EAST),
                block.getRelative(BlockFace.SOUTH),
                block.getRelative(BlockFace.WEST)
        );
    }

    private AbilityBreakBlockEvent callAbilityBreakBlockEvent(Player player, RealItem item, Block block) {
        AbilityBreakBlockEvent event = new AbilityBreakBlockEvent(
                player,
                this,
                item,
                block
        );
        System.out.println("Calling event");
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
}
