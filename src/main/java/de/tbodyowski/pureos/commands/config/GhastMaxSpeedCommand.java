package de.tbodyowski.pureos.commands.config;

import de.tbodyowski.pureos.Main;
import de.tbodyowski.pureos.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class GhastMaxSpeedCommand implements SubCommand {
    @Override
    public String getName() {
        return "ghastmaxspeed";
    }

    @Override
    public String getDescription() {
        return "Setze den maximalen GhastSpeed";
    }

    @Override
    public String getUsage() {
        return "/config ghastmaxspeed <value>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 1){
            sender.sendMessage(ChatColor.RED + "Verwendung: " + getUsage());
            return true;
        }
        try {
            double value = Double.parseDouble(args[0]);
            if (value <=0){
                sender.sendMessage(ChatColor.RED + "Wert muss größer sein als 0");
                return true;
            }

            Main.getInstance().getConfig().set("MaxGhastSpeed",value);
            Main.getInstance().saveConfig();
            Main.getInstance().reloadConfig();

            sender.sendMessage(ChatColor.GREEN + "GhastMaxSpeed wurde auf " + ChatColor.WHITE + value + ChatColor.GREEN + " gesetzt!");
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Zahl ist ungültig");
        }
        return true;
    }
}
