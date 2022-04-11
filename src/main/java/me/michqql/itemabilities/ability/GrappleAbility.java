package me.michqql.itemabilities.ability;

import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.ItemAbility;
import me.michqql.itemabilities.item.ItemGenerator;
import me.michqql.itemabilities.item.ItemModifier;
import me.michqql.itemabilities.util.Pair;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class GrappleAbility extends ItemAbility {

    public GrappleAbility(ItemAbilityPlugin plugin) {
        super(plugin, "grapple");
    }

    @EventHandler
    public void onFishingRodCast(PlayerFishEvent e) {
        // Player must be holding correct item in hand
        Pair<Boolean, ItemStack> item = isPlayerHoldingCorrectItem(e.getPlayer(), false);
        if(!item.key)
            return;

        // Do not care about this state, only reel in states
        if(e.getState() == PlayerFishEvent.State.FISHING)
            return;

        // States: REEL_IN   (player has not caught anything, was pulled in),
        //         IN_GROUND (reel landed on ground, was pulled in)
        if(e.getState() != PlayerFishEvent.State.REEL_IN && e.getState() != PlayerFishEvent.State.IN_GROUND) {
            e.setCancelled(true);
            return;
        }

        ItemModifier.getAndSetPropertyValue(item.value, "uses", (value) -> {
            int remaining = value.intValue() - 1;
            if(remaining <= 0) {
                // break item
                return null;
            }
            System.out.println("Remaining uses: " + remaining);
            return remaining;
        });
        ItemGenerator.updateItem(item.value);

        Location hook = e.getHook().getLocation().clone();
        //hook.setY(inverseLerp(0, 255, hook.getY()) - 1); // trying to normalize y-value
        Location result = hook.subtract(e.getPlayer().getLocation());
        e.getPlayer().setVelocity(result.toVector().normalize().multiply(2D).multiply(new Vector(1D, 1.5D, 1D)));
    }

    private double inverseLerp(double min, double max, double value) {
        return (value - min) / (max - min);
    }
}
