package de.tbodyowski.waros.AntiCheat.checks.movement;

import de.tbodyowski.waros.AntiCheat.Data.DataPlayer;
import de.tbodyowski.waros.AntiCheat.checks.Check;
import de.tbodyowski.waros.AntiCheat.checks.CheckType;
import de.tbodyowski.waros.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

public class Fly extends Check {
    public Fly(String name, CheckType type, boolean enabled, boolean punishable, int max) {
        super(name, type, enabled, punishable, max);
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove (PlayerMoveEvent event){
        DataPlayer data = Main.getInstance().getDataManager().getDataPlayer(event.getPlayer());
        if (data == null
            || event.getPlayer().getAllowFlight()
            || event.getPlayer().getVehicle() != null
            || data.inLiquid
            || data.onClimbable
            || System.currentTimeMillis() - data.lastVelocityTaken < 200L) return;

        float deltaY = (float) (event.getTo().getY() - event.getFrom().getY());

        if (data.airTicks > 2 && !data.onStairSlab && !data.nearGround && deltaY > data.lastDeltaY){
            if (data.flyThreshold++ > 2){
                flag(event.getPlayer(), "deltaY: " + deltaY);
            }
        }else data.flyThreshold-= data.flyThreshold > 0 ? 0.1 : 0;

        float accel = (deltaY - data.lastDeltaY);

        if(data.airTicks > 1 && Math.abs(accel) < 0.01){
            if(data.flyThreshold++ > 3){
                flag(event.getPlayer(), "accel: " + accel);
            }
        }else data.flyThreshold-= data.flyThreshold > 0 ? 0.25f : 0;
        data.lastAccel = accel;
        data.lastDeltaY = deltaY;
    }
}
