package me.michqql.itemabilities.enchant.enchants;

import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.enchant.EnchantWrapper;
import me.michqql.itemabilities.item.NKeyCache;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CorruptionEnchant extends EnchantWrapper {

    private final static String KEY = "corruption-percentage";

    public CorruptionEnchant(ItemAbilityPlugin plugin) {
        super(plugin, "corruption");
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Player player))
            return;

        if(!isPlayerHoldingCorrectItem(player, true)) // Swords can only be used in main hand
            return;

        Entity entity = e.getEntity();
        if(!(entity instanceof LivingEntity living))
            return;

        final NamespacedKey key = NKeyCache.getNKey(KEY);

        PersistentDataContainer data = living.getPersistentDataContainer();
        int corruptionPercentage = data.getOrDefault(key, PersistentDataType.INTEGER, 0);
        corruptionPercentage += getEnchantmentLevel(player, EquipmentSlot.HAND);

        if(corruptionPercentage >= getIntSetting("corruption_threshold", 10)) {
            data.remove(key);
            // do extra damage to the mob
            living.damage(Math.max(getDoubleSetting("minimum_damage", 10.0D), living.getHealth() / 2));
            player.playSound(player, Sound.ENTITY_WITCH_DEATH, 0.8f, 1.1f);
        } else {
            data.set(key, PersistentDataType.INTEGER, corruptionPercentage);
            player.playSound(player, Sound.ENTITY_WITCH_HURT, 0.25f, 0.4f);
        }
        World world = player.getWorld();
        world.spawnParticle(Particle.SPELL_WITCH,
                living.getLocation().clone().add(0, living.getHeight() / 2, 0),
                5, 0.5D, 0.25D, 0.5D);
    }
}
