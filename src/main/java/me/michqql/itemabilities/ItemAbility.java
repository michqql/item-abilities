package me.michqql.itemabilities;

import me.michqql.itemabilities.data.DataFile;
import me.michqql.itemabilities.data.JsonFile;
import me.michqql.itemabilities.item.ItemModifier;
import me.michqql.itemabilities.util.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public abstract class ItemAbility implements Listener {

    protected final ItemAbilitiesPlugin plugin;
    protected final JsonFile jsonFile;
    protected final String identifier;

    public ItemAbility(ItemAbilitiesPlugin plugin, String identifier) {
        this.plugin = plugin;
        this.jsonFile = new JsonFile(plugin, new DataFile.Path("items", identifier, "json"));
        this.identifier = identifier;

        ItemAbilityHandler.registerAbility(this);
    }

    public String getIdentifier() {
        return identifier;
    }

    protected boolean isCorrectItem(ItemStack itemStack) {
        return ItemModifier.isItemOfType(plugin, itemStack, identifier);
    }

    protected Pair<Boolean, ItemStack> isPlayerHoldingCorrectItem(Player player, boolean mainHandOnly) {
        EntityEquipment equipment = player.getEquipment();
        if(equipment == null)
            return new Pair<>(false, null);

        boolean heldInMainHand = ItemModifier.isItemOfType(plugin, equipment.getItemInMainHand(), identifier);
        if(mainHandOnly || heldInMainHand) {
            return new Pair<>(heldInMainHand, equipment.getItemInMainHand());
        } else {
            boolean heldInOffHand = ItemModifier.isItemOfType(plugin, equipment.getItemInOffHand(), identifier);
            return new Pair<>(heldInOffHand, equipment.getItemInOffHand());
        }
    }
}
