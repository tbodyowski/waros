package de.tbodyowski.pureos.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class AdminChatCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    public static final Set<Player> adminChatToggled = new HashSet<>();
    public AdminChatCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Nur Spieler können diesen Befehl benutzen!");
            return true;
        }
        if (args.length == 0) {
            if (adminChatToggled.contains(player)) {
                adminChatToggled.remove(player);
                player.sendMessage(ChatColor.RED + "Du bist nun nicht mehr im Adminchat");
            }else {
                adminChatToggled.add(player);
                player.sendMessage(ChatColor.RED + "Du bist nun im Adminchat");
            }
            return true;
        }
        String message = String.join(" ", args);
        sendAdminChat(player, message);
        return true;
    }
    public static void sendAdminChat(Player sender, String message) {

        sender.getServer().getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("adminchat.use"))
                .forEach(p -> p.sendMessage("§4[AdminChat] §f" + sender.getName() + ": " + message));

        sender.getServer().getConsoleSender().sendMessage("[AdminChat] " + sender.getName() + ": " + message);
    }
}
