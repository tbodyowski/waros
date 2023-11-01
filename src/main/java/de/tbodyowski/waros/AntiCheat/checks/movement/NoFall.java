package de.tbodyowski.waros.AntiCheat.checks.movement;

import de.tbodyowski.waros.AntiCheat.Data.DataPlayer;
import de.tbodyowski.waros.AntiCheat.checks.Check;
import de.tbodyowski.waros.AntiCheat.checks.CheckType;
import de.tbodyowski.waros.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class NoFall extends Check {
    public NoFall(String name, CheckType type, boolean enabled, boolean punishable, int max) {
        super(name, type, enabled, punishable, max);
    }

    private static double groundY = 1 / 64.;
    private int buffer = 0;

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        DataPlayer data = Main.getInstance().getDataManager().getDataPlayer(event.getPlayer());

        boolean clientGround = event.getPlayer().isOnGround(),
                serverGround = event.getTo().getY() % groundY < 0.0001;

        if (clientGround != data.lServerGround) {
            if (++buffer > 1 || !data.nearGround || !data.lServerGround) {
                flag(event.getPlayer(), "c=" + clientGround, "s=" + data.lServerGround,
                        "y=" + event.getFrom().getY(), "gy=" + event.getFrom().getY() % groundY);
            }
        } else if (buffer > 0) buffer --;

        data.lServerGround = serverGround;
    }
}
