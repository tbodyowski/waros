package de.tbodyowski.waros.Events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class DeathEvent implements Listener {


    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        event.getDrops().add(PlayerHead(event.getPlayer()));
    }
    public ItemStack PlayerHead(Player player){
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(player);
        skull.setItemMeta(meta);
        return skull;
    }
}
