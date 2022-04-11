package me.michqql.itemabilities.commands.player;

import me.michqql.core.util.MessageHandler;
import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.commands.SubCommand;
import me.michqql.itemabilities.reclaim.ReclaimHandler;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReclaimSubCommand extends SubCommand {

    private final ReclaimHandler reclaimHandler;

    public ReclaimSubCommand(ItemAbilityPlugin plugin, MessageHandler messageHandler, ReclaimHandler reclaimHandler) {
        super(plugin, messageHandler);
        this.reclaimHandler = reclaimHandler;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // /ia reclaim
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // /ia reclaim
        return null;
    }

    @Override
    public String getName() {
        return "reclaim";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}
