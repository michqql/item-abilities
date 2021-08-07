package me.michqql.itemabilities.commands;

import me.michqql.itemabilities.ItemAbilitiesPlugin;
import me.michqql.itemabilities.item.ItemCompare;
import me.michqql.itemabilities.item.ItemGenerator;
import me.michqql.itemabilities.item.reclaim.ReclaimHandler;
import me.michqql.itemabilities.util.CommandUtil;
import me.michqql.itemabilities.util.MessageUtil;
import me.michqql.itemabilities.util.NameToUUIDConverter;
import me.michqql.itemabilities.util.Perm;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class ItemAbilityCommand implements CommandExecutor {

    private final ItemAbilitiesPlugin plugin;
    private final MessageUtil msg;

    private final ReclaimHandler reclaimHandler;

    public ItemAbilityCommand(ItemAbilitiesPlugin plugin, MessageUtil msg, ReclaimHandler reclaimHandler) {
        this.plugin = plugin;
        this.msg = msg;
        this.reclaimHandler = reclaimHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] fullArguments) {
        if(!(sender instanceof Player)) {
            msg.sendList(sender, "requires-player");
            return true;
        }

        final Player player = (Player) sender;
        final boolean isAdmin = player.hasPermission(Perm.ADMIN);

        if(fullArguments.length == 0) {
            HashMap<String, String> placeholders = new HashMap<String, String>(){{ put("command", label); }};
            msg.sendList(player, "command-help", placeholders);
            if(isAdmin)
                msg.sendList(player, "admin-command-help", placeholders);
            return true;
        }

        final String subCommand = fullArguments[0];
        final String[] args = Arrays.copyOfRange(fullArguments, 1, fullArguments.length);
        // Admin commands
        if(subCommand.equalsIgnoreCase("give")) {
            if(!isAdmin) {
                msg.sendList(sender, "no-permission", new HashMap<String, String>(){{
                    put("permission", Perm.ADMIN);
                }});
                return true;
            }

            if(args.length < 1) {
                msg.sendList(player, "incomplete-command", new HashMap<String, String>(){{
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
                msg.sendList(player, "item-not-found", new HashMap<String, String>(){{
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
                UUID uuid = NameToUUIDConverter.getUUIDFromName(name);
                if(uuid == null) {
                    msg.sendList(player, "player-not-found", new HashMap<String, String>(){{
                        put("player", name);
                    }});
                    return true;
                }

                reclaimHandler.giveItem(uuid, itemId, amount);
                msg.sendList(player, "give-item.admin-offline", new HashMap<String, String>(){{
                    put("item.id", itemId);
                    put("item.name", ItemCompare.getItemName(item));
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
                put("item.name", ItemCompare.getItemName(item));
                put("amount", String.valueOf(amount));
                put("sender", player.getName());
                put("receiver", finalTarget.getName());
            }};

            msg.sendList(player, "give-item.admin", placeholders);
            if(!player.equals(target)) {
                if(sentToReclaim)
                    msg.sendList(target, "give-item.player-reclaim", placeholders);
                else
                    msg.sendList(target, "give-item.player", placeholders);
            }
        }

        // Player commands
        if(subCommand.equalsIgnoreCase("reclaim")) {
            int[] amounts = reclaimHandler.reclaimItems(player);

            msg.sendList(player, "reclaim", new HashMap<String, String>(){{
                put("amount.reclaimed", String.valueOf(amounts[0]));
                put("amount.remain", String.valueOf(amounts[1]));
                put("plural.reclaimed", (amounts[0] != 1 ? "s" : ""));
                put("plural.remain", (amounts[1] != 1 ? "s" : ""));
            }});
        }
        return true;
    }


}
