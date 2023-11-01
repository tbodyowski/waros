package de.tbodyowski.waros.Events;

import de.tbodyowski.waros.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Main.getInstance().getDataManager().add(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Main.getInstance().getDataManager().remove(event.getPlayer());
    }
}
