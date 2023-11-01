package de.tbodyowski.waros.Events;

import org.bukkit.event.Listener;

public class FrameEvent implements Listener {

    /*@EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.ITEM_FRAME) {
            if (event.getItemInHand().getItemMeta().getDisplayName().equals("Invisible Item Frame")) {
                ItemFrame im = event.getBlock().getWorld().spawn(event.getBlock().getLocation(), ItemFrame.class);
                im.setVisible(false);
                ItemStack frame = im.getItem();
                event.getBlock().setBlockData((BlockData) frame.getData());
            }
        }
    }
    */
}
