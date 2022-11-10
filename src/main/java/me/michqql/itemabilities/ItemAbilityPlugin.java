package me.michqql.itemabilities;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.michqql.core.gui.GuiHandler;
import me.michqql.core.io.CommentFile;
import me.michqql.core.util.MessageHandler;
import me.michqql.itemabilities.ability.GrappleAbility;
import me.michqql.itemabilities.ability.TreeFallerAbility;
import me.michqql.itemabilities.commands.ItemAbilityCommand;
import me.michqql.itemabilities.enchant.enchants.CorruptionEnchant;
import me.michqql.itemabilities.enchant.enchants.TelekenesisEnchant;
import me.michqql.itemabilities.item.ClientboundItemSetter;
import me.michqql.itemabilities.item.ItemUUIDTracker;
import me.michqql.itemabilities.reclaim.ReclaimHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class ItemAbilityPlugin extends JavaPlugin {

    // Static
    public static final String ADMIN_PERMISSION = "ia.admin";

    private static ItemAbilityPlugin instance;

    public static ItemAbilityPlugin getInstance() {
        return instance;
    }
    public static String getPluginName() {
        return instance.getName();
    }
    public static Logger getLog() { return instance.getLogger(); }
    // End of static

    private ProtocolManager protocolManager;
    private GuiHandler guiHandler;

    private MessageHandler messageHandler;
    private ReclaimHandler reclaimHandler;

    @Override
    public void onEnable() {
        instance = this; // static instance
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        Settings.loadSettings(this);
        ItemUUIDTracker.init();

        final CommentFile languageFile = new CommentFile(this, "", "lang");
        this.messageHandler = new MessageHandler(languageFile.getConfig());
        this.guiHandler = new GuiHandler(this);

        this.reclaimHandler = new ReclaimHandler(this);

        registerAbilities();
        registerEnchantments();
        registerCommand();

        // Handles updating item properties when sending packets to the client
        new ClientboundItemSetter(this);
    }

    @Override
    public void onDisable() {
        ItemUUIDTracker.shutdown();
    }

    private void registerAbilities() {
        new GrappleAbility(this);
        new TreeFallerAbility(this);
    }

    private void registerEnchantments() {
        new TelekenesisEnchant(this);
        new CorruptionEnchant(this);
    }

    private void registerCommand() {
        PluginCommand command = getCommand("itemAbility");
        if(command == null) {
            Bukkit.getLogger().warning("[ItemAbility] Could not register command! (It's null?)");
            return;
        }

        ItemAbilityCommand itemAbilityCommand = new ItemAbilityCommand(this, messageHandler, guiHandler, reclaimHandler);
        command.setExecutor(itemAbilityCommand);
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}