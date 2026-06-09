package de.tbodyowski.pureos.commands;

import de.tbodyowski.pureos.manager.InventorySeeManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EndseeCommand implements CommandExecutor, TabCompleter {

    private final InventorySeeManager inventorySeeManager;

    public EndseeCommand(InventorySeeManager inventorySeeManager) {
        this.inventorySeeManager = inventorySeeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player viewer)) {
            sender.sendMessage("Nur Spieler koennen diesen Befehl nutzen.");
            return true;
        }

        if (args.length != 1) {
            viewer.sendMessage("§cNutze: /endsee <spieler>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.isOnline() && !target.hasPlayedBefore()) {
            viewer.sendMessage("§cSpieler nicht gefunden.");
            return true;
        }

        inventorySeeManager.openEndSee(viewer, target);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }

        String prefix = args[0].toLowerCase();
        List<String> matches = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getName().toLowerCase().startsWith(prefix)) {
                matches.add(online.getName());
            }
        }
        return matches;
    }
}

