package de.tbodyowski.pureos.Events;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HorseRideSpeedEvent implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Double> originalHorseSpeed = new HashMap<>();

    public HorseRideSpeedEvent(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHorseMount(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player)) {
            return;
        }
        if (!(event.getVehicle() instanceof Horse horse)) {
            return;
        }

        AttributeInstance movement = horse.getAttribute(Attribute.MOVEMENT_SPEED);
        if (movement == null) {
            return;
        }

        UUID horseId = horse.getUniqueId();
        if (!originalHorseSpeed.containsKey(horseId)) {
            originalHorseSpeed.put(horseId, movement.getBaseValue());
        }

        double multiplier = Math.max(1.0D, plugin.getConfig().getDouble("HorseRideSpeedMultiplier", 2.0D));
        double base = originalHorseSpeed.get(horseId);
        movement.setBaseValue(base * multiplier);
    }

    @EventHandler
    public void onHorseDismount(VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player)) {
            return;
        }
        if (!(event.getVehicle() instanceof Horse horse)) {
            return;
        }

        resetHorseSpeed(horse);
    }

    @EventHandler
    public void onHorseDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Horse horse) {
            originalHorseSpeed.remove(horse.getUniqueId());
        }
    }

    private void resetHorseSpeed(Horse horse) {
        AttributeInstance movement = horse.getAttribute(Attribute.MOVEMENT_SPEED);
        if (movement == null) {
            return;
        }

        Double original = originalHorseSpeed.remove(horse.getUniqueId());
        if (original != null) {
            movement.setBaseValue(original);
        }
    }
}

