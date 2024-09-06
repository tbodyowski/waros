package de.tbodyowski.waros.commands;

import de.tbodyowski.waros.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /openinv <inventory>");
            return true;
        }
        switch (args[0].toLowerCase()){
            case "guild":
                player.openInventory(Main.getInstance().getGuildAdminInventory().getGAdminInv());
                break;
        }
        return false;
    }
}
