package me.michqql.itemabilities;

import me.michqql.core.io.CommentFile;
import me.michqql.core.util.MessageHandler;
import me.michqql.itemabilities.ability.GrappleAbility;
import me.michqql.itemabilities.ability.TreeFallerAbility;
import me.michqql.itemabilities.commands.ItemAbilityCommand;
import me.michqql.itemabilities.item.reclaim.ReclaimHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ItemAbilitiesPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        final CommentFile languageFile = new CommentFile(this, "", "lang");

        final ReclaimHandler reclaimHandler = new ReclaimHandler(this);
        final MessageHandler messageHandler = new MessageHandler(languageFile.getConfig());

        registerAbilities();

        // Commands
        Objects.requireNonNull(getCommand("item_ability")).setExecutor(new ItemAbilityCommand(this, messageHandler, reclaimHandler));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerAbilities() {
        new GrappleAbility(this);
        new TreeFallerAbility(this);
    }
}
