package de.tbodyowski.waros.Events;

import de.tbodyowski.waros.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GUIClickEvent implements Listener {
    private final Map<Player, String> playerGuildMap = new HashMap<>();

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        ItemStack clickedItem = event.getCurrentItem();
        String displayName = clickedItem.getItemMeta().getDisplayName();

        if (event.getClickedInventory().equals(Main.getInstance().getGuildAdminInventory().getGAdminInv())) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();

            if (displayName != null && !displayName.isEmpty()) {
                player.openInventory(Main.getInstance().getGuildAdminInventory().getGuildView(displayName));
                playerGuildMap.put(player, displayName); // Store the display name
            } else {
                player.sendMessage(ChatColor.RED + "Invalid guild name.");
            }
            return;
        }

        if (event.getClickedInventory().equals(Main.getInstance().getGuildAdminInventory().getGuildView(displayName))) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack itemAtSlot4 = event.getClickedInventory().getItem(4);
            String itemName = itemAtSlot4.getItemMeta().getDisplayName();

            if (clickedItem.getType() == Material.PLAYER_HEAD) {
                player.openInventory(Main.getInstance().getGuildAdminInventory().getGuildPlayersView(displayName));
            } else if (clickedItem.getType() == Material.BARRIER && event.getClick() == ClickType.DOUBLE_CLICK) {
                String guildName = playerGuildMap.get(player); // Retrieve the stored display name
                if (guildName != null) {
                    Main.getInstance().getGuildManager().deleteGuildAdmin(guildName);
                    event.getClickedInventory().clear();
                    player.closeInventory();
                    player.sendMessage(ChatColor.GREEN + "The guild " + ChatColor.RED + guildName + ChatColor.GREEN + " has been deleted.");
                }
            }
            return;
        }
    }
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        playerGuildMap.remove(player);
    }
}
