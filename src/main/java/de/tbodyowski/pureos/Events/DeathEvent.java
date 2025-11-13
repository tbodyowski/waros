package de.tbodyowski.pureos.Events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeathEvent implements Listener {


    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        event.getDrops().add(PlayerHead(event.getPlayer()));
    }
    public ItemStack PlayerHead(Player player){
        List<String> lore = new ArrayList<>();
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(player);

        if (player.getKiller() != null) {
            lore.add("§7Getötet von: §e" + player.getKiller().getName());
        }
        meta.setLore(lore);
        skull.setItemMeta(meta);
        return skull;
    }
}
