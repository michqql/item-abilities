package me.michqql.itemabilities.commands.admin;

import me.michqql.core.gui.Gui;
import me.michqql.core.gui.GuiHandler;
import me.michqql.core.item.ItemBuilder;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.core.util.text.CenteredText;
import me.michqql.itemabilities.ItemAbility;
import me.michqql.itemabilities.ItemAbilityHandler;
import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.commands.SubCommand;
import me.michqql.itemabilities.item.ItemGenerator;
import me.michqql.itemabilities.item.ItemModifier;
import me.michqql.itemabilities.item.NKeyCache;
import me.michqql.itemabilities.item.item.Property;
import me.michqql.itemabilities.item.item.RealItem;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ViewItemSubCommand extends SubCommand {

    private final GuiHandler guiHandler;

    public ViewItemSubCommand(ItemAbilityPlugin plugin, MessageHandler messageHandler, GuiHandler guiHandler) {
        super(plugin, messageHandler);
        this.guiHandler = guiHandler;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // Open a GUI, displaying two versions of the item
        // Version 1 has no placeholders replaced (it is an 'example' item)
        // Version 2 has placeholders replaced

        // /itemAbility view |<item_id>|
        if(args.length == 0) {
            messageHandler.sendList(sender, "incomplete-command",
                    Placeholder.of("command", "ia view <item id>"));
            return;
        }

        String itemId = args[0];
        ItemAbility itemAbility = ItemAbilityHandler.getAbility(itemId);
        if(itemAbility == null) {
            messageHandler.sendList(sender, "item-not-found", Placeholder.of("id", itemId));
            return;
        }

        new ViewItemGui(guiHandler, (Player) sender, itemId).openGui();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // /itemAbility view |<item_id>|
        // args[0] = item

        // No input, suggest all
        if(args.length == 0) {
            List<String> result = new ArrayList<>();
            Collection<ItemAbility> abilities = ItemAbilityHandler.getAbilities();
            for(ItemAbility ability : abilities) {
                result.add(ability.getIdentifier());
            }
            return result;
        }

        // Suggest abilities starting with the input
        if(args.length == 1) {
            String start = args[0].toLowerCase(Locale.ROOT);
            List<String> result = new ArrayList<>();
            Collection<ItemAbility> abilities = ItemAbilityHandler.getAbilities();
            for(ItemAbility ability : abilities) {
                if(ability.getIdentifier().toLowerCase(Locale.ROOT).startsWith(start))
                    result.add(ability.getIdentifier());
            }
            return result;
        }
        return null;
    }

    @Override
    public String getName() {
        return "view";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public String getPermission() {
        return ItemAbilityPlugin.ADMIN_PERMISSION;
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

    static class ViewItemGui extends Gui {

        private final static int ORIGINAL_ITEM_SLOT = 12;
        private final static int DATA_SLOT = 13;
        private final static int EXAMPLE_ITEM_SLOT = 14;

        private final ItemStack item;

        public ViewItemGui(GuiHandler guiHandler, Player player, String itemId) {
            super(guiHandler, player);
            this.item = ItemGenerator.generateItem(itemId);
            build("&2Viewing &f" + ItemModifier.getItemName(item), 3);
        }

        @Override
        protected void createInventory() {
            this.inventory.setItem(ORIGINAL_ITEM_SLOT, item);

            // Create example item
            ItemStack copy = item.clone();
            ItemMeta meta = copy.getItemMeta();
            if(meta != null) {
                PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
                dataContainer.set(NKeyCache.getNKey(ItemGenerator.EXAMPLE_KEY), PersistentDataType.STRING, "true");
            }
            copy.setItemMeta(meta);

            this.inventory.setItem(EXAMPLE_ITEM_SLOT, copy);

            // Data
            RealItem data = ItemModifier.getItemData(copy);
            List<String> lore = new ArrayList<>();
            lore.add("&eItem ID: &f" + data.getItemId());
            lore.add("&eItem UUID: &f" + (data.getItemUniqueId() != null ?
                    (data.isExampleItem() ? "&cNONE" : data.getItemUniqueId().toString()) : "&cNULL"));
            lore.add("&eIs Example: &f" + (data.isExampleItem() ? "&atrue" : "&cfalse"));
            List<Property> properties = data.getProperties();
            lore.add("&eProperties: (" + properties.size() + ")");
            for (Property property : properties) {
                lore.add("  &f" + property.getTag() + " = " + property.getValue().intValue());
            }
            lore.add("&eEnchantable: " + (data.isEnchantable() ? "&atrue" : "&cfalse"));
            lore.add("&eReforgable: " + (data.isReforgable() ? "&atrue" : "&cfalse"));
            this.inventory.setItem(DATA_SLOT, new ItemBuilder(Material.BOOK)
                    .displayName("&2Item Data").lore(CenteredText.forItemLore(lore, 100)).getItem());
        }

        @Override
        protected void updateInventory() {}

        @Override
        protected void onClose() {}

        @Override
        protected boolean onClickEvent(int i, ClickType clickType) {
            return true;
        }

        @Override
        protected boolean onPlayerInventoryClickEvent(int i, ClickType clickType) {
            return true;
        }
    }
}
