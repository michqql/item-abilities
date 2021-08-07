package me.michqql.itemabilities.ability;

import me.michqql.itemabilities.ItemAbilitiesPlugin;
import me.michqql.itemabilities.ItemAbility;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.util.Vector;

public class GrappleAbility extends ItemAbility {

    public GrappleAbility(ItemAbilitiesPlugin plugin) {
        super(plugin, "grapple");
    }

    @EventHandler
    public void onFishingRodCast(PlayerFishEvent e) {
        // Player must be holding correct item in hand
        if(!isPlayerHoldingCorrectItem(e.getPlayer(), false))
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

        Location hook = e.getHook().getLocation().clone();
        //hook.setY(inverseLerp(0, 255, hook.getY()) - 1); // trying to normalize y-value
        Location result = hook.subtract(e.getPlayer().getLocation());
        e.getPlayer().setVelocity(result.toVector().normalize().multiply(2D).multiply(new Vector(1D, 1.5D, 1D)));
    }

    private double inverseLerp(double min, double max, double value) {
        return (value - min) / (max - min);
    }
}
