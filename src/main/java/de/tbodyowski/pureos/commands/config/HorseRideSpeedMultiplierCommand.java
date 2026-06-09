package de.tbodyowski.pureos.commands.config;

import de.tbodyowski.pureos.Main;
import de.tbodyowski.pureos.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class HorseRideSpeedMultiplierCommand implements SubCommand {
    @Override
    public String getName() {
        return "horseridespeedmultiplier";
    }

    @Override
    public String getDescription() {
        return "Setze den Pferde-Reitgeschwindigkeits-Multiplikator";
    }

    @Override
    public String getUsage() {
        return "/config horseridespeedmultiplier <value>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Verwendung: " + getUsage());
            return true;
        }

        try {
            double value = Double.parseDouble(args[0]);
            if (value <= 0) {
                sender.sendMessage(ChatColor.RED + "Wert muss größer sein als 0");
                return true;
            }

            Main.getInstance().getConfig().set("HorseRideSpeedMultiplier", value);
            Main.getInstance().saveConfig();
            Main.getInstance().reloadConfig();

            sender.sendMessage(ChatColor.GREEN + "HorseRideSpeedMultiplier wurde auf " + ChatColor.WHITE + value + ChatColor.GREEN + " gesetzt!");
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Zahl ist ungültig");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length != 1) {
            return List.of();
        }

        String prefix = args[0].toLowerCase();
        List<String> suggestions = new ArrayList<>();
        for (String suggestion : List.of("1.0", "1.5", "2.0", "3.0", "5.0", "10.0")) {
            if (suggestion.startsWith(prefix)) {
                suggestions.add(suggestion);
            }
        }
        return suggestions;
    }
}

