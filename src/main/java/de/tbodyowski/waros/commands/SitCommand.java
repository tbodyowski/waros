package de.tbodyowski.waros.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class SitCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (player.isInsideVehicle() && player.getVehicle() instanceof ArmorStand) {
            player.sendMessage("Du sitzt bereits, stehe auf um erneut zu sitzen");
            return true;
        }

        Location loc = player.getLocation();

        ArmorStand armorStand = loc.getWorld().spawn(loc, ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setMarker(true);
        armorStand.setSmall(true);
        armorStand.setInvulnerable(true);
        armorStand.setCustomName(player.getName() + "'s seat");
        armorStand.setCustomNameVisible(false);
        armorStand.addPassenger(player);

        player.sendMessage("Du sitzt nun");

        return true;
    }
}
