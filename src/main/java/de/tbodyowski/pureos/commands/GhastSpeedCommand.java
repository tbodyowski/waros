package de.tbodyowski.pureos.commands;

import de.tbodyowski.pureos.Main;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HappyGhast;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GhastSpeedCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED+"Nur Spieler können das");
        }

        Player player = (Player) sender;

        Entity vehicle = player.getVehicle();
        if (vehicle == null || !(vehicle instanceof HappyGhast)){
            player.sendMessage(ChatColor.RED+"Du musst auf einem Happy Ghast sitzen");
            return true;
        }
        HappyGhast ghast = (HappyGhast) vehicle;


        if (args.length == 0){
            Main.getInstance().getGhastSpeedInventory().openGhastSpeedInventory(player, ghast);
            return true;
        }
        if (args.length != 1){
            player.sendMessage(ChatColor.RED+"Nutze: /ghastspeed <speed>");
            return true;
        }
        double newSpeed;
        try {
            newSpeed = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED+"Ungültiger Wert");
            return true;
        }

        AttributeInstance attr = ghast.getAttribute(Attribute.FLYING_SPEED);
        if (attr == null){
            player.sendMessage(ChatColor.RED+"Dieser Happy Ghast kann nicht schneller werden");
        }
        if (newSpeed > Main.getInstance().getConfig().getDouble("MaxGhastSpeed")) {
            player.sendMessage(ChatColor.RED + "Ein Ghast kann nicht schneller als " + ChatColor.WHITE + Main.getInstance().getConfig().getDouble("MaxGhastSpeed") + ChatColor.RED + " werden!");
            return true;
        }
        attr.setBaseValue(0.05*newSpeed);
        player.sendMessage(ChatColor.GREEN + "Dein Ghast hat nun eine Geschwindigkeit von " + ChatColor.GOLD + newSpeed);

        return true;
    }
}
