package de.tbodyowski.pureos.Events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class LightBlockBreakEvent implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        Block block = event.getBlock();
        if (block.getType() == Material.LIGHT){
           event.setDropItems(false);
           event.setCancelled(true);

           block.setType(Material.AIR);

           ItemStack lightBlock = new ItemStack(Material.LIGHT, 1);

           block.getWorld().dropItem(block.getLocation(), lightBlock);
        }
    }
}
