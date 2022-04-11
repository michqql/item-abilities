package me.michqql.itemabilities.commands.admin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.itemabilities.ItemAbility;
import me.michqql.itemabilities.ItemAbilityHandler;
import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.commands.SubCommand;
import me.michqql.itemabilities.item.ItemGenerator;
import me.michqql.itemabilities.reclaim.ReclaimHandler;
import me.michqql.itemabilities.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GiveSubCommand extends SubCommand {

    private final static UUID CONSOLE_UUID = UUID.randomUUID();
    private final static long ONE_MONTH = TimeUnit.DAYS.toMillis(30);
    private final Cache<UUID, UUID> senderToReceiverCache = CacheBuilder.newBuilder()
            .maximumSize(50).expireAfterWrite(1, TimeUnit.MINUTES).build();

    private final ReclaimHandler reclaimHandler;

    public GiveSubCommand(ItemAbilityPlugin plugin, MessageHandler messageHandler, ReclaimHandler reclaimHandler) {
        super(plugin, messageHandler);
        this.reclaimHandler = reclaimHandler;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // /itemAbility give |<player> <item_id> [amount]|
        if(args.length < 2) {
            messageHandler.sendList(sender, "incomplete-command",
                    Placeholder.of("command", "ia give <player> <item id> [amount]"));
            return;
        }

        OfflinePlayer player = CommandUtil.parseOfflinePlayer(args, 0);
        if(player == null) {
            messageHandler.sendList(sender, "player-not-found", Placeholder.of("player", args[0]));
            return;
        }
        final String playerName = player.getName() == null ? args[0] : player.getName();

        // If player has not played before, or has not played recently
        if(!player.hasPlayedBefore() || player.getLastPlayed() + ONE_MONTH < System.currentTimeMillis()) {
            // If cache does not contain sender, or UUID of cache doesn't equal this player
            UUID ofSender = sender instanceof Player playerSender ? playerSender.getUniqueId() : CONSOLE_UUID;
            UUID inCache = senderToReceiverCache.getIfPresent(ofSender);

            if(inCache == null || inCache != player.getUniqueId()) {
                // Inform sender of this and ask them to resend command to confirm
                senderToReceiverCache.put(ofSender, player.getUniqueId());

                if(player.hasPlayedBefore()) {
                    long timeDifference = System.currentTimeMillis() - player.getLastPlayed();
                    messageHandler.sendList(sender, "give-item.not-played-since", new HashMap<>(){{
                        put("player.name", playerName);
                        put("last-played", convertMillis(timeDifference));
                    }});
                } else {
                    messageHandler.sendList(sender, "give-item.never-played", new HashMap<>() {{
                        put("player.name", playerName);
                    }});
                }
                return;
            }
        }

        String itemId = args[1];
        ItemAbility itemAbility = ItemAbilityHandler.getAbility(itemId);
        if(itemAbility == null) {
            messageHandler.sendList(sender, "item-not-found", Placeholder.of("id", itemId));
            return;
        }

        int amount = CommandUtil.parseInt(args, 2, 1);

        // Get item
        ItemStack item;
        try {
            item = ItemGenerator.generateItem(itemId);
        } catch(IllegalArgumentException e) {
            messageHandler.sendList(sender, "item-not-found", new HashMap<>() {{
                put("id", itemId);
            }});
            return;
        }
        item.setAmount(amount);

        // Give item to player
        // If online and inventory is full, add to reclaim
        // If offline, add to reclaim
        // If online, add to inventory
        if(player.isOnline()) {
            Player online = player.getPlayer();
            assert online != null; // Checked by isOnline

            // Attempts to add to inventory
            HashMap<Integer, ItemStack> result = online.getInventory().addItem(item);
            if(result.size() > 0) {
                // Inventory is full, add to reclaim
                ItemStack stack = result.get(0);
                reclaimHandler.giveItem(online.getUniqueId(), itemAbility.getIdentifier(), stack.getAmount());

                messageHandler.sendList(sender, "give-item.player-reclaim", new HashMap<>(){{
                    put("amount", String.valueOf(amount));
                    put("item.name", itemAbility.getIdentifier());
                    put("item_name", itemAbility.getIdentifier());
                    put("item-name", itemAbility.getIdentifier());
                }});
            } else {
                messageHandler.sendList(sender, "give-item.player", new HashMap<>(){{
                    put("amount", String.valueOf(amount));
                    put("item.name", itemAbility.getIdentifier());
                    put("item_name", itemAbility.getIdentifier());
                    put("item-name", itemAbility.getIdentifier());
                }});
            }

            messageHandler.sendList(sender, "give-item.admin", new HashMap<>(){{
                put("receiver", playerName);
                put("amount", String.valueOf(amount));
                put("item.id", itemAbility.getIdentifier());
                put("item_id", itemAbility.getIdentifier());
                put("item-id", itemAbility.getIdentifier());
            }});
        } else {
            reclaimHandler.giveItem(player.getUniqueId(), itemAbility.getIdentifier(), amount);

            messageHandler.sendList(sender, "give-item.admin-offline", new HashMap<>(){{
                put("receiver", playerName);
                put("amount", String.valueOf(amount));
                put("item.id", itemAbility.getIdentifier());
                put("item_id", itemAbility.getIdentifier());
                put("item-id", itemAbility.getIdentifier());
            }});
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // /itemAbility give |<player> <item_id> [amount]|
        // args[0] = player, args[1] = item, args[2] = amount
        switch (args.length) {
            case 1: return CommandUtil.matchPlayerNames(args[0]);

            case 2:
                String start = args[1].toLowerCase(Locale.ROOT);
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
        return "give";
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
        return false;
    }

    private final static long ONE_DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);
    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.0");
    private String convertMillis(long millis) {
        if(millis == -1)
            return ChatColor.RED + "NEVER";

        double days = (double) millis / ONE_DAY_IN_MILLIS;
        return DECIMAL_FORMAT.format(days) + " days ago";
    }
}
