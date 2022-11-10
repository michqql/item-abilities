package me.michqql.itemabilities.commands;

import me.michqql.core.gui.GuiHandler;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.commands.admin.EnchantSubCommand;
import me.michqql.itemabilities.commands.admin.GiveSubCommand;
import me.michqql.itemabilities.commands.admin.ViewItemSubCommand;
import me.michqql.itemabilities.commands.player.ReclaimSubCommand;
import me.michqql.itemabilities.reclaim.ReclaimHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemAbilityCommand implements CommandExecutor, TabExecutor {

    private final ItemAbilityPlugin plugin;
    private final MessageHandler messageHandler;
    private final GuiHandler guiHandler;

    private final ReclaimHandler reclaimHandler;

    private final List<SubCommand> registeredSubCommands = new ArrayList<>();

    public ItemAbilityCommand(ItemAbilityPlugin plugin, MessageHandler messageHandler,
                              GuiHandler guiHandler, ReclaimHandler reclaimHandler) {
        this.plugin = plugin;
        this.messageHandler = messageHandler;
        this.guiHandler = guiHandler;
        this.reclaimHandler = reclaimHandler;
        registerSubCommands();
    }

    private void registerSubCommands() {
        registeredSubCommands.addAll(Arrays.asList(
                // Admin commands
                new GiveSubCommand(plugin, messageHandler, reclaimHandler),
                new ViewItemSubCommand(plugin, messageHandler, guiHandler),
                new EnchantSubCommand(plugin, messageHandler),

                // Player commands
                new ReclaimSubCommand(plugin, messageHandler, reclaimHandler)
        ));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        boolean isPlayer = sender instanceof Player;
        boolean isAdmin = sender.hasPermission(ItemAbilityPlugin.ADMIN_PERMISSION);

        // Send help message, for both player and admins
        if(args.length == 0) {
            HashMap<String, String> placeholders = new HashMap<String, String>(){{ put("command", label); }};
            messageHandler.sendList(sender, "command-help", placeholders);
            if(isAdmin)
                messageHandler.sendList(sender, "admin-command-help", placeholders);
            return true;
        }

        // Get the sub command
        SubCommand subCommand = getSubCommand(args[0]);
        if(subCommand == null) {
            messageHandler.sendList(sender, "invalid-command", Placeholder.of("command", args[0]));
            return true;
        }

        // Copy the array, removing the sub command string (the first index)
        String[] argsCopy = Arrays.copyOfRange(args, 1, args.length);

        // Check sender has permission
        if(subCommand.getPermission() != null && !subCommand.getPermission().isEmpty() &&
                !sender.hasPermission(subCommand.getPermission())) {
            messageHandler.sendList(sender, "no-permission");
            return true;
        }

        // Check the sender is of required type
        if(subCommand.requiresPlayer() && !isPlayer) {
            messageHandler.sendList(sender, "requires-player");
            return true;
        }

        // Execute the command
        subCommand.onCommand(sender, argsCopy);
        return true;
    }

    //    @Override
//    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] fullArguments) {
//        if(!(sender instanceof Player)) {
//            messageHandler.sendList(sender, "requires-player");
//            return true;
//        }
//
//        final Player player = (Player) sender;
//        final boolean isAdmin = player.hasPermission(Perm.ADMIN);
//
//        if(fullArguments.length == 0) {
//            HashMap<String, String> placeholders = new HashMap<String, String>(){{ put("command", label); }};
//            messageHandler.sendList(player, "command-help", placeholders);
//            if(isAdmin)
//                messageHandler.sendList(player, "admin-command-help", placeholders);
//            return true;
//        }
//
//        final String subCommand = fullArguments[0];
//        final String[] args = Arrays.copyOfRange(fullArguments, 1, fullArguments.length);
//        // Admin commands
//        if(subCommand.equalsIgnoreCase("give")) {
//            if(!isAdmin) {
//                messageHandler.sendList(sender, "no-permission", new HashMap<String, String>(){{
//                    put("permission", Perm.ADMIN);
//                }});
//                return true;
//            }
//
//            if(args.length < 1) {
//                messageHandler.sendList(player, "incomplete-command", new HashMap<String, String>(){{
//                    put("command", label + " give <item_id> [amount] [player_name]");
//                }});
//                return true;
//            }
//
//            String itemId = args[0];
//            int amount = CommandUtil.parseInt(args, 1, 1);
//            Player target = CommandUtil.parseOnlinePlayer(args, 2);
//
//            ItemStack item;
//            try {
//                item = ItemGenerator.generateItem(plugin, itemId);
//            } catch(IllegalArgumentException e) {
//                messageHandler.sendList(player, "item-not-found", new HashMap<String, String>(){{
//                    put("id", itemId);
//                }});
//                return true;
//            }
//            item.setAmount(amount);
//
//            if(target == null && args.length < 3) {
//                target = player;
//            } else if (target == null) {
//                // Add item to reclaim
//                String name = args[2];
//                UUID uuid = OfflineUUID.getUUID(name);
//                if(uuid == null) {
//                    messageHandler.sendList(player, "player-not-found", new HashMap<String, String>(){{
//                        put("player", name);
//                    }});
//                    return true;
//                }
//
//                reclaimHandler.giveItem(uuid, itemId, amount);
//                messageHandler.sendList(player, "give-item.admin-offline", new HashMap<String, String>(){{
//                    put("item.id", itemId);
//                    put("item.name", ItemModifier.getItemName(item));
//                    put("amount", String.valueOf(amount));
//                    put("sender", player.getName());
//                    put("receiver", name);
//                }});
//                return true;
//            }
//
//            boolean sentToReclaim = false;
//            if(target.getInventory().firstEmpty() != -1)
//                target.getInventory().addItem(item);
//            else {
//                reclaimHandler.giveItem(target.getUniqueId(), itemId, amount);
//                sentToReclaim = true;
//            }
//
//            final Player finalTarget = target;
//            HashMap<String, String> placeholders = new HashMap<String, String>(){{
//                put("item.id", itemId);
//                put("item.name", ItemModifier.getItemName(item));
//                put("amount", String.valueOf(amount));
//                put("sender", player.getName());
//                put("receiver", finalTarget.getName());
//            }};
//
//
//            if(!player.equals(target))
//                messageHandler.sendList(player, "give-item.admin", placeholders);
//
//            if(sentToReclaim)
//                messageHandler.sendList(target, "give-item.player-reclaim", placeholders);
//            else
//                messageHandler.sendList(target, "give-item.player", placeholders);
//        }
//
//        // Player commands
//        if(subCommand.equalsIgnoreCase("reclaim")) {
//            int[] amounts = reclaimHandler.reclaimItems(player);
//
//            messageHandler.sendList(player, "reclaim", new HashMap<String, String>(){{
//                put("amount.reclaimed", String.valueOf(amounts[0]));
//                put("amount.remain", String.valueOf(amounts[1]));
//                put("plural.reclaimed", (amounts[0] != 1 ? "s" : ""));
//                put("plural.remain", (amounts[1] != 1 ? "s" : ""));
//            }});
//        }
//        return true;
//    }


    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        boolean isPlayer = sender instanceof Player;

        // Get the sub command
        String input = args[0];
        SubCommand subCommand = getSubCommand(input);
        if(subCommand == null) {
            // No sub command match, suggest sub commands
            List<String> result = new ArrayList<>();
            for(SubCommand sub : registeredSubCommands) {
                if(sub.getName().startsWith(input))
                    result.add(sub.getName());

                List<String> aliases = sub.getAliases();
                if(aliases == null || aliases.isEmpty())
                    continue;

                for(String alias : aliases) {
                    if(alias.startsWith(input))
                        result.add(alias);
                }
            }
            return result;
        }

        // Copy the array, removing the sub command string (the first index)
        String[] argsCopy = Arrays.copyOfRange(args, 1, args.length);

        // Check sender has permission
        if(subCommand.getPermission() != null && !subCommand.getPermission().isEmpty() &&
                !sender.hasPermission(subCommand.getPermission())) {
            return null;
        }

        // Check the sender is of required type
        if(subCommand.requiresPlayer() && !isPlayer) {
            return null;
        }

        // Execute the command
        List<String> result = subCommand.onTabComplete(sender, argsCopy);
        return result == null ? Collections.emptyList() : result;
    }

    private SubCommand getSubCommand(String string) {
        for(SubCommand subCommand : registeredSubCommands) {
            if(subCommand.getName().equalsIgnoreCase(string))
                return subCommand;

            List<String> aliases = subCommand.getAliases();
            if(aliases == null || aliases.isEmpty())
                continue;

            for(String alias : aliases) {
                if(alias.equalsIgnoreCase(string))
                    return subCommand;
            }
        }
        return null;
    }
}
