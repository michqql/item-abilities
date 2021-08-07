package me.michqql.itemabilities;

import me.michqql.itemabilities.ability.GrappleAbility;
import me.michqql.itemabilities.commands.ItemAbilityCommand;
import me.michqql.itemabilities.data.CommentFile;
import me.michqql.itemabilities.data.DataFile;
import me.michqql.itemabilities.util.MessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ItemAbilitiesPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        final MessageUtil messageUtil = new MessageUtil(new CommentFile(this, new DataFile.Path("", "lang", "yml")));

        registerAbilities();

        // Commands
        Objects.requireNonNull(getCommand("item_ability")).setExecutor(new ItemAbilityCommand(this, messageUtil));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerAbilities() {
        new GrappleAbility(this);
    }
}
