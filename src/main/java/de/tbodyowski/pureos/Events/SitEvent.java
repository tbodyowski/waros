package de.tbodyowski.pureos.Events;

import de.tbodyowski.pureos.Main;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;

public class SitEvent implements Listener {


    @EventHandler
    public void onPlayerDismount(EntityDismountEvent event) {
        if (event.getDismounted() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getDismounted();

            Main.getInstance().getServer().getScheduler().runTaskLater(Main.getInstance(), armorStand::remove, 1L);
        }
    }
}
