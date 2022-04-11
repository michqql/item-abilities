package me.michqql.itemabilities;

import me.michqql.core.io.CommentFile;
import me.michqql.core.util.MessageHandler;
import me.michqql.itemabilities.ability.GrappleAbility;
import me.michqql.itemabilities.ability.TreeFallerAbility;
import me.michqql.itemabilities.commands.ItemAbilityCommand;
import me.michqql.itemabilities.reclaim.ReclaimHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ItemAbilityPlugin extends JavaPlugin {

    // Static
    public static final String ADMIN_PERMISSION = "ia.admin";

    private static ItemAbilityPlugin instance;

    public static ItemAbilityPlugin getInstance() {
        return instance;
    }
    // End of static

    private MessageHandler messageHandler;
    private ReclaimHandler reclaimHandler;

    @Override
    public void onEnable() {
        instance = this; // static instance

        final CommentFile languageFile = new CommentFile(this, "", "lang");
        this.messageHandler = new MessageHandler(languageFile.getConfig());

        this.reclaimHandler = new ReclaimHandler(this);

        registerAbilities();
        registerCommand();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerAbilities() {
        new GrappleAbility(this);
        new TreeFallerAbility(this);
    }

    private void registerCommand() {
        PluginCommand command = getCommand("itemAbility");
        if(command == null) {
            Bukkit.getLogger().warning("[ItemAbility] Could not register command! (It's null?)");
            return;
        }

        ItemAbilityCommand itemAbilityCommand = new ItemAbilityCommand(this, messageHandler, reclaimHandler);
        command.setExecutor(itemAbilityCommand);
    }
}
