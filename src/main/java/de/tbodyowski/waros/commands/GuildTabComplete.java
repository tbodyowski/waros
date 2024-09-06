package de.tbodyowski.waros.commands;

import de.tbodyowski.waros.manager.GuildManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuildTabComplete implements TabCompleter {

    private final GuildManager guildManager;

    public GuildTabComplete(GuildManager guildManager) {
        this.guildManager = guildManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender;

        // Handle different commands and arguments
        if (args.length == 1) {
            // First argument for /guild command
            if (args[0].isEmpty()) {
                return Arrays.asList("create", "invite", "accept", "leave", "list", "delete");
            }
        } else if (args.length == 2) {
            // Second argument for specific commands
            if ("invite".equalsIgnoreCase(args[0]) || "accept".equalsIgnoreCase(args[0])) {
                List<String> guilds = new ArrayList<>(guildManager.getAllGuildNames());
                return filterList(guilds, args[1]);
            } else if ("delete".equalsIgnoreCase(args[0])) {
                // List guilds for deletion
                List<String> guilds = new ArrayList<>(guildManager.getAllGuildNames());
                return filterList(guilds, args[1]);
            }
        }

        return null;
    }

    private List<String> filterList(List<String> list, String prefix) {
        List<String> result = new ArrayList<>();
        for (String item : list) {
            if (item.toLowerCase().startsWith(prefix.toLowerCase())) {
                result.add(item);
            }
        }
        return result;
    }
}