package me.michqql.itemabilities.commands;

import me.michqql.core.util.MessageHandler;
import me.michqql.itemabilities.ItemAbilityPlugin;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class SubCommand {

    protected final ItemAbilityPlugin plugin;
    protected final MessageHandler messageHandler;

    public SubCommand(ItemAbilityPlugin plugin, MessageHandler messageHandler) {
        this.plugin = plugin;
        this.messageHandler = messageHandler;
    }

    public abstract void onCommand(CommandSender sender, String[] args);
    public abstract List<String> onTabComplete(CommandSender sender, String[] args);

    public abstract String getName();
    public abstract List<String> getAliases();
    public abstract String getPermission();
    public abstract boolean requiresPlayer();
}
