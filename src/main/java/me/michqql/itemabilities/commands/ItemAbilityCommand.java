package me.michqql.itemabilities.commands;

import me.michqql.itemabilities.ItemAbilitiesPlugin;
import me.michqql.itemabilities.data.DataFile;
import me.michqql.itemabilities.data.JsonFile;
import me.michqql.itemabilities.item.ItemGenerator;
import me.michqql.itemabilities.util.CommandUtil;
import me.michqql.itemabilities.util.MessageUtil;
import me.michqql.itemabilities.util.Perm;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;

/*
 * TODO:
 *  - Allow giving items to offline players (add to inventory on join event/storage command)
 *  - Check if player has space for given items (otherwise add to storage)
 */
public class ItemAbilityCommand implements CommandExecutor {

    private final ItemAbilitiesPlugin plugin;
    private final MessageUtil msg;

    public ItemAbilityCommand(ItemAbilitiesPlugin plugin, MessageUtil messageUtil) {
        this.plugin = plugin;
        this.msg = messageUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] fullArguments) {
        if(!(sender instanceof Player)) {
            msg.sendList(sender, "requires-player");
            return true;
        }

        Player player = (Player) sender;

        if(!player.hasPermission(Perm.ADMIN)) {
            msg.sendList(sender, "no-permission", new HashMap<String, String>(){{
                put("permission", Perm.ADMIN);
            }});
            return true;
        }

        if(fullArguments.length == 0) {
            msg.sendList(player, "admin-command-help", new HashMap<String, String>(){{
                put("command", label);
            }});
            return true;
        }

        String subCommand = fullArguments[0];
        String[] args = Arrays.copyOfRange(fullArguments, 1, fullArguments.length);
        if(subCommand.equalsIgnoreCase("give")) {
            if(args.length < 1) {
                msg.sendList(player, "incomplete-command", new HashMap<String, String>(){{
                    put("command", label + " give <item_id> [amount] [player_name]");
                }});
                return true;
            }

            String itemId = args[0];
            int amount = CommandUtil.parseInt(args, 1, 1);
            Player target = CommandUtil.parseOnlinePlayer(args, 2);
            if(target == null && args.length >= 3) {
                msg.sendList(player, "player-offline", new HashMap<String, String>(){{
                    put("player", args[2]);
                }});
                return true;
            } else if(target == null) {
                target = player;
            }

            ItemStack item;
            try {
                item = ItemGenerator.generateItem(plugin, new JsonFile(plugin, new DataFile.Path("items", itemId, "json")));
            } catch(IllegalArgumentException e) {
                msg.sendList(player, "item-not-found", new HashMap<String, String>(){{
                    put("id", itemId);
                }});
                return true;
            }

            item.setAmount(amount);
            target.getInventory().addItem(item);

            final Player finalTarget = target;
            HashMap<String, String> placeholders = new HashMap<String, String>(){{
                put("item.id", itemId);
                put("item.name", "");
                put("amount", String.valueOf(amount));
                put("sender", player.getName());
                put("receiver", finalTarget.getName());
            }};

            msg.sendList(player, "give-item.admin", placeholders);
            if(!player.equals(target))
                msg.sendList(target, "give-item.player", placeholders);
        }
        return true;
    }


}
