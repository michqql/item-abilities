package me.michqql.itemabilities;

import me.michqql.itemabilities.data.DataFile;
import me.michqql.itemabilities.data.JsonFile;
import me.michqql.itemabilities.item.ItemCompare;
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

        ItemAbilityManager.registerAbility(this);
    }

    public String getIdentifier() {
        return identifier;
    }

    protected boolean isCorrectItem(ItemStack itemStack) {
        return ItemCompare.isItemOfType(plugin, itemStack, identifier);
    }

    protected boolean isPlayerHoldingCorrectItem(Player player, boolean mainHandOnly) {
        EntityEquipment equipment = player.getEquipment();
        if(equipment == null)
            return false;

        boolean heldInMainHand = ItemCompare.isItemOfType(plugin, equipment.getItemInMainHand(), identifier);
        return mainHandOnly ? heldInMainHand : (heldInMainHand || ItemCompare.isItemOfType(plugin, equipment.getItemInOffHand(), identifier));
    }
}
