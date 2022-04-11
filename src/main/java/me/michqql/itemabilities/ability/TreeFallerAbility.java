package me.michqql.itemabilities.ability;

import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.ItemAbility;
import me.michqql.itemabilities.item.ItemGenerator;
import me.michqql.itemabilities.item.ItemModifier;
import me.michqql.itemabilities.util.Pair;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TreeFallerAbility extends ItemAbility {

    private final static List<Material> WOOD_BLOCKS = Arrays.asList(
            Material.ACACIA_LOG,
            Material.BIRCH_LOG,
            Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG,
            Material.OAK_LOG,
            Material.SPRUCE_LOG
    );

    private final int MAX_BLOCK_UPDATES_PER_TICK = 4;

    public TreeFallerAbility(ItemAbilityPlugin plugin) {
        super(plugin, "tree_faller");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        // Player must be holding correct item in hand
        Pair<Boolean, ItemStack> item = isPlayerHoldingCorrectItem(e.getPlayer(), false);
        if(!item.key)
            return;

        Block block = e.getBlock();

        if(!isBlockWood(block)) {
            return;
        }

        e.setCancelled(true);

        ItemModifier.getAndSetPropertyValue(item.value, "uses", (value) -> {
            int remaining = value.intValue() - 1;
            if(remaining <= 0) {
                // break item
                return null;
            }
            System.out.println("Remaining uses: " + remaining);
            return remaining;
        });
        ItemGenerator.updateItem(item.value);

        final World world = block.getWorld();
        new BukkitRunnable() {

            int blockUpdates;
            Block current = block;
            final Queue<Block> toVisit = new LinkedList<>();

            @Override
            public void run() {
                blockUpdates = 0;

                checkBlock(current);

                while(toVisit.size() > 0 && blockUpdates < MAX_BLOCK_UPDATES_PER_TICK) {
                    current = toVisit.poll();
                    checkBlock(current);
                }
            }

            private void checkBlock(Block block) {
                if(isBlockWood(current)) {
                    block.breakNaturally();
                    world.playSound(block.getLocation(), Sound.BLOCK_LAVA_POP, 1.0f, 1.5f);
                    blockUpdates++;
                }

                for(Block b : getRelativeBlocks(current)) {
                    if(isBlockWood(b) && !toVisit.contains(b))
                        toVisit.add(b);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private boolean isBlockWood(Block block) {
        return WOOD_BLOCKS.contains(block.getType());
    }

    private List<Block> getRelativeBlocks(Block block) {
        return Arrays.asList(
                block.getRelative(BlockFace.UP),
                block.getRelative(BlockFace.DOWN),
                block.getRelative(BlockFace.NORTH),
                block.getRelative(BlockFace.EAST),
                block.getRelative(BlockFace.SOUTH),
                block.getRelative(BlockFace.WEST)
        );
    }
}
