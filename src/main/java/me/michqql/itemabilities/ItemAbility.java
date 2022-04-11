package me.michqql.itemabilities;

import me.michqql.core.io.JsonFile;
import me.michqql.itemabilities.item.ItemModifier;
import me.michqql.itemabilities.util.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public abstract class ItemAbility implements Listener {

    protected final ItemAbilityPlugin plugin;
    protected final JsonFile jsonFile;
    protected final String identifier;

    public ItemAbility(ItemAbilityPlugin plugin, String identifier) {
        this.plugin = plugin;
        this.jsonFile = new JsonFile(plugin, "items", identifier);
        this.identifier = identifier;

        ItemAbilityHandler.registerAbility(this);
    }

    public String getIdentifier() {
        return identifier;
    }

    protected boolean isCorrectItem(ItemStack itemStack) {
        return ItemModifier.isItemOfType(itemStack, identifier);
    }

    protected Pair<Boolean, ItemStack> isPlayerHoldingCorrectItem(Player player, boolean mainHandOnly) {
        EntityEquipment equipment = player.getEquipment();
        if(equipment == null)
            return new Pair<>(false, null);

        boolean heldInMainHand = ItemModifier.isItemOfType(equipment.getItemInMainHand(), identifier);
        if(mainHandOnly || heldInMainHand) {
            return new Pair<>(heldInMainHand, equipment.getItemInMainHand());
        } else {
            boolean heldInOffHand = ItemModifier.isItemOfType(equipment.getItemInOffHand(), identifier);
            return new Pair<>(heldInOffHand, equipment.getItemInOffHand());
        }
    }
}
