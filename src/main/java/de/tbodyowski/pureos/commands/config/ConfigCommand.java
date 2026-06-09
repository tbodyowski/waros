package de.tbodyowski.pureos.commands.config;

import de.tbodyowski.pureos.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ConfigCommand implements CommandExecutor, TabCompleter {

    private Map<String, SubCommand> subCommands = new HashMap<>();

    public ConfigCommand() {
        register(new GhastMaxSpeedCommand());
        register(new HorseRideSpeedMultiplierCommand());
    }
    private void register(SubCommand s){
        subCommands.put(s.getName().toLowerCase(), s);
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Verfügbare SubCommands:");
            subCommands.values().forEach(s ->
                    sender.sendMessage("§b" + s.getUsage() + "§7" + s.getDescription()));
            return true;
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand == null) {
            sender.sendMessage(ChatColor.RED + "Unbekannter Command. Nutze /config für Hilfe.!");
            return true;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        return subCommand.execute(sender, subArgs);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return subCommands.keySet()
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .sorted()
                    .toList();
        }
        SubCommand sub =  subCommands.get(args[0].toLowerCase());
        if (sub == null) return Collections.emptyList();

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        return sub.tabComplete(sender, subArgs);
    }
}
