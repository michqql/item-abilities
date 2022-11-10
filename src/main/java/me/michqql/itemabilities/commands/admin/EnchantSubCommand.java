package me.michqql.itemabilities.commands.admin;

import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.commands.SubCommand;
import me.michqql.itemabilities.enchant.EnchantHandler;
import me.michqql.itemabilities.enchant.EnchantWrapper;
import me.michqql.itemabilities.item.ItemModifier;
import me.michqql.itemabilities.util.CommandUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EnchantSubCommand extends SubCommand {

    public EnchantSubCommand(ItemAbilityPlugin plugin, MessageHandler messageHandler) {
        super(plugin, messageHandler);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // ia enchant <enchantment> [level]
        if(args.length == 0) {
            messageHandler.sendList(sender, "incomplete-command",
                    Placeholder.of("command", "ia enchant <enchantment> [level]"));
            return;
        }

        if(CommandUtil.hasFlag(args, 0, "-l")) {
            messageHandler.sendList(sender, "enchant-item.list-enchants.title");
            for(EnchantWrapper wrapper : EnchantHandler.getEnchants()) {
                messageHandler.sendList(sender, "enchant-item.list-enchants.body",
                        Placeholder.of("id", wrapper.getEnchantId(), "name", wrapper.getKey().getKey()));
            }
            return;
        }

        String enchantId = args[0];
        EnchantWrapper enchantment = EnchantHandler.getEnchant(enchantId);
        if(enchantment == null) {
            messageHandler.sendList(sender, "enchant-not-found", Placeholder.of("id", enchantId));
            return;
        }

        int level = CommandUtil.parseInt(args, 1, 1);

        ItemStack hand = ((Player) sender).getInventory().getItemInMainHand();
        if(hand.getType().isAir()) {
            messageHandler.sendList(sender, "enchant-item.must-hold-item");
            return;
        }

        if(!ItemModifier.getItemData(hand).isEnchantable()) {
            messageHandler.sendList(sender, "enchant-item.item-cannot-be-enchanted");
            return;
        }

        hand.addEnchantment(enchantment, level);

        String itemName = hand.getItemMeta() != null && hand.getItemMeta().hasDisplayName() ?
                hand.getItemMeta().getDisplayName() : hand.getType().toString();
        messageHandler.sendList(sender,"enchant-item.enchanted",
                Placeholder.of(
                        "item", itemName,
                        "enchantment", enchantment.getEnchantId(),
                        "level", Integer.toString(level)
                ));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if(args.length == 1) {
            List<String> enchantments = new ArrayList<>();
            for(EnchantWrapper wrapper : EnchantHandler.getEnchants()) {
                enchantments.add(wrapper.getEnchantId());
            }
            return enchantments;
        }
        return null;
    }

    @Override
    public String getName() {
        return "enchant";
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
}
