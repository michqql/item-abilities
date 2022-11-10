package me.michqql.itemabilities.event;

import me.michqql.itemabilities.ItemAbility;
import me.michqql.itemabilities.item.item.RealItem;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AbilityBreakBlockEvent extends Event {

    private final Player player;
    private final ItemAbility ability;
    private final RealItem item;
    private final Block block;

    private boolean blockDrop = true;

    public AbilityBreakBlockEvent(Player player, ItemAbility ability, RealItem item, Block block) {
        this.player = player;
        this.ability = ability;
        this.item = item;
        this.block = block;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemAbility getAbility() {
        return ability;
    }

    public RealItem getItem() {
        return item;
    }

    public Block getBlock() {
        return block;
    }

    public boolean isBlockDrop() {
        return blockDrop;
    }

    public void setBlockDrop(boolean blockDrop) {
        this.blockDrop = blockDrop;
    }

    /* Handlers */
    private final static HandlerList HANDLER_LIST = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
