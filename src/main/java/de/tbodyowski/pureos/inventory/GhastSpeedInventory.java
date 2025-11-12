package de.tbodyowski.pureos.inventory;

import de.tbodyowski.pureos.Main;
import de.tbodyowski.pureos.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.data.type.DriedGhast;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HappyGhast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GhastSpeedInventory implements Listener {

    private static final String GUI_TITLE = "§8Ghast Speed Control";

    public void openGhastSpeedInventory(Player player, HappyGhast ghast) {
        Inventory inventory = Bukkit.createInventory(null, 3*9, GUI_TITLE);
        double current = getMultiplier((HappyGhast) ghast);

        inventory.setItem(11, button(Material.RED_CONCRETE, "§c-1.0", "Verringern um 1.0"));
        inventory.setItem(12, button(Material.RED_CONCRETE, "§c-0.1", "Verringern um 0.1"));
        inventory.setItem(13, ghastItem(current));
        inventory.setItem(14, button(Material.LIME_CONCRETE, "§a+0.1", "Erhöhen um 0.1"));
        inventory.setItem(15, button(Material.LIME_CONCRETE, "§a+1.0", "Erhöhen um 1.0"));
        inventory.setItem(22, button(Material.BARRIER, "§7Reset", "Zurücksetzen"));

        player.openInventory(inventory);
    }
    private ItemStack button(Material mat, String name, String lore){
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of("§7" + lore));
        item.setItemMeta(meta);
        return item;
    }
    private ItemStack ghastItem(double multiplier) {
        ItemStack item = new ItemStack(Material.DRIED_GHAST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f§lHappy Ghast");
        double actualSpeed = 0.05 * multiplier;
        meta.setLore(List.of(
                "§7Aktuelle Geschwindigkeit: §e" + String.format("%.2f", multiplier) + "x"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private double getMultiplier(HappyGhast ghast) {
        AttributeInstance attr = ghast.getAttribute(Attribute.FLYING_SPEED);
        if (attr == null) return 1.0;
        return attr.getBaseValue() / 0.05;
    }

    private void setMultiplier(HappyGhast ghast, double multiplier) {
        AttributeInstance attr = ghast.getAttribute(Attribute.FLYING_SPEED);
        if (attr != null) attr.setBaseValue(0.05 * multiplier);
    }
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(GUI_TITLE)) {
            e.setCancelled(true);
            if (!(e.getWhoClicked() instanceof Player player)) return;
            Entity vehicle = player.getVehicle();
            if (!(vehicle instanceof HappyGhast ghast)) {
                player.closeInventory();
                player.sendMessage("§cDu bist nicht mehr auf einem Happy Ghast!");
                return;
            }

            double multiplier = getMultiplier(ghast);

            switch (e.getSlot()) {
                case 11 -> multiplier -= 1.0;
                case 12 -> multiplier -= 0.1;
                case 14 -> multiplier += 0.1;
                case 15 -> multiplier += 1.0;
                case 22 -> multiplier = 1.0;
                default -> { return; }
            }

            if (multiplier < 0.1) multiplier = 0.1;
            if (multiplier > Main.getInstance().getConfig().getInt("MaxGhastSpeed")) multiplier = Main.getInstance().getConfig().getInt("MaxGhastSpeed");

            setMultiplier(ghast, multiplier);
            openGhastSpeedInventory(player, ghast);
            player.sendMessage("§aGhast Speed ist nun bei §e" + String.format("%.2f", multiplier) + "§a!");
        }
    }
}
