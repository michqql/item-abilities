package me.michqql.itemabilities.commands;

import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.OfflineUUID;
import me.michqql.itemabilities.ItemAbilitiesPlugin;
import me.michqql.itemabilities.item.ItemModifier;
import me.michqql.itemabilities.item.ItemGenerator;
import me.michqql.itemabilities.item.reclaim.ReclaimHandler;
import me.michqql.itemabilities.util.CommandUtil;
import me.michqql.itemabilities.util.Perm;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class ItemAbilityCommand implements CommandExecutor {

    private final ItemAbilitiesPlugin plugin;
    private final MessageHandler messageHandler;

    private final ReclaimHandler reclaimHandler;

    public ItemAbilityCommand(ItemAbilitiesPlugin plugin, MessageHandler messageHandler, ReclaimHandler reclaimHandler) {
        this.plugin = plugin;
        this.messageHandler = messageHandler;
        this.reclaimHandler = reclaimHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] fullArguments) {
        if(!(sender instanceof Player)) {
            messageHandler.sendList(sender, "requires-player");
            return true;
        }

        final Player player = (Player) sender;
        final boolean isAdmin = player.hasPermission(Perm.ADMIN);

        if(fullArguments.length == 0) {
            HashMap<String, String> placeholders = new HashMap<String, String>(){{ put("command", label); }};
            messageHandler.sendList(player, "command-help", placeholders);
            if(isAdmin)
                messageHandler.sendList(player, "admin-command-help", placeholders);
            return true;
        }

        final String subCommand = fullArguments[0];
        final String[] args = Arrays.copyOfRange(fullArguments, 1, fullArguments.length);
        // Admin commands
        if(subCommand.equalsIgnoreCase("give")) {
            if(!isAdmin) {
                messageHandler.sendList(sender, "no-permission", new HashMap<String, String>(){{
                    put("permission", Perm.ADMIN);
                }});
                return true;
            }

            if(args.length < 1) {
                messageHandler.sendList(player, "incomplete-command", new HashMap<String, String>(){{
                    put("command", label + " give <item_id> [amount] [player_name]");
                }});
                return true;
            }

            String itemId = args[0];
            int amount = CommandUtil.parseInt(args, 1, 1);
            Player target = CommandUtil.parseOnlinePlayer(args, 2);

            ItemStack item;
            try {
                item = ItemGenerator.generateItem(plugin, itemId);
            } catch(IllegalArgumentException e) {
                messageHandler.sendList(player, "item-not-found", new HashMap<String, String>(){{
                    put("id", itemId);
                }});
                return true;
            }
            item.setAmount(amount);

            if(target == null && args.length < 3) {
                target = player;
            } else if (target == null) {
                // Add item to reclaim
                String name = args[2];
                UUID uuid = OfflineUUID.getUUID(name);
                if(uuid == null) {
                    messageHandler.sendList(player, "player-not-found", new HashMap<String, String>(){{
                        put("player", name);
                    }});
                    return true;
                }

                reclaimHandler.giveItem(uuid, itemId, amount);
                messageHandler.sendList(player, "give-item.admin-offline", new HashMap<String, String>(){{
                    put("item.id", itemId);
                    put("item.name", ItemModifier.getItemName(item));
                    put("amount", String.valueOf(amount));
                    put("sender", player.getName());
                    put("receiver", name);
                }});
                return true;
            }

            boolean sentToReclaim = false;
            if(target.getInventory().firstEmpty() != -1)
                target.getInventory().addItem(item);
            else {
                reclaimHandler.giveItem(target.getUniqueId(), itemId, amount);
                sentToReclaim = true;
            }

            final Player finalTarget = target;
            HashMap<String, String> placeholders = new HashMap<String, String>(){{
                put("item.id", itemId);
                put("item.name", ItemModifier.getItemName(item));
                put("amount", String.valueOf(amount));
                put("sender", player.getName());
                put("receiver", finalTarget.getName());
            }};


            if(!player.equals(target))
                messageHandler.sendList(player, "give-item.admin", placeholders);

            if(sentToReclaim)
                messageHandler.sendList(target, "give-item.player-reclaim", placeholders);
            else
                messageHandler.sendList(target, "give-item.player", placeholders);
        }

        // Player commands
        if(subCommand.equalsIgnoreCase("reclaim")) {
            int[] amounts = reclaimHandler.reclaimItems(player);

            messageHandler.sendList(player, "reclaim", new HashMap<String, String>(){{
                put("amount.reclaimed", String.valueOf(amounts[0]));
                put("amount.remain", String.valueOf(amounts[1]));
                put("plural.reclaimed", (amounts[0] != 1 ? "s" : ""));
                put("plural.remain", (amounts[1] != 1 ? "s" : ""));
            }});
        }
        return true;
    }


}
