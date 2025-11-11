package de.tbodyowski.pureos.commands;

import de.tbodyowski.pureos.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishCommand implements CommandExecutor {

    private final Set<UUID> vanishedPlayers = new HashSet<>();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = null;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Please specify a player name when running from console.");
                return true;
            }


            player = (Player) sender;

        }else {
            player = Bukkit.getPlayer(args[0]);

            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Player" + player.getName() + " not found.");
                return true;
            }
        }

        boolean isVanished = vanishedPlayers.contains(player.getUniqueId());

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(player)) continue;

            if (isVanished)
                p.showPlayer(Main.getInstance(), player);
            else
                p.hidePlayer(Main.getInstance(), player);
                }
        sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() +  "is now " + (isVanished ? "visible" : "invisible"));

        if (isVanished)
            vanishedPlayers.remove(player.getUniqueId());
        else
            vanishedPlayers.add(player.getUniqueId());

        return true;
    }
}
