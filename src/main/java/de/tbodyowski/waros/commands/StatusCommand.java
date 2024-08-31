package de.tbodyowski.waros.commands;

import de.tbodyowski.waros.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import de.tbodyowski.waros.manager.PrefixManager;

import static de.tbodyowski.waros.manager.FileManager.*;

public class StatusCommand implements CommandExecutor {

    YamlConfiguration statusData = Main.getInstance().getFileManager().getStatusData();
    private int Prefix_LengthLimit = Main.getInstance().getConfig().getInt("Prefix-LengthLimit");

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length == 0) {
                sendUsage(sender);
                return true;
            }

            switch (args[0].toLowerCase()) { //status ___
                case "help" -> sendUsage(p);
                case "reset" -> {
                    switch (args.length){
                        case 1 ->{
                            if (!statusData.getString(p.getUniqueId() + ".status").equals("Default")) {
                                savePlayerInStatus(p, "Default", "§f");
                                PrefixManager.updatePrefix(p);
                                p.sendMessage(Main.getInstance().getStatus_Prefix() +
                                        "§7Dein Status wurde §9zurück §7gesetzt!");
                            } else {
                                p.sendMessage(Main.getInstance().getStatus_Prefix() +
                                        "§7Dein Status ist schon auf §9Default§7!");
                            }
                        }
                        case 2 ->{
                            if (p.hasPermission("status.admin")) {
                                Player target = Bukkit.getPlayerExact(args[1]);
                                assert target != null;
                                if (playerIsRegistered(target)) {
                                    savePlayerInStatus(target, "Default", "§f");
                                    PrefixManager.updatePrefix(target);
                                    p.sendMessage(Main.getInstance().getStatus_Prefix()
                                            + "§7Der Status von§9 "
                                            + target.getName()
                                            + "§7 wurde §9zurück §7gesetzt!");
                                } else {
                                    p.sendMessage(Main.getInstance().getStatus_Prefix() +
                                            "§9Dieser Spieler wurde noch nicht registriert!");
                                }
                            } else {
                                p.sendMessage(Main.getInstance().getStatus_Prefix() +
                                        "§9Du darfst dies nicht!");
                            }
                        }
                        default -> sendUsage(p);
                    }
                }
                case "set" -> {
                    Boolean UppercaseLengthLimitToggle = Main.getInstance().getConfig().getBoolean("Uppercase/LengthLimit-Toggle");
                    switch (args.length){
                        case 2 ->{
                            if (UppercaseLengthLimitToggle) {
                                if (args[1].length() >= Prefix_LengthLimit) {
                                    p.sendMessage(Main.getInstance().getStatus_Prefix() +
                                            "§7Dieser Status ist §9zu lang§7 (max " + Prefix_LengthLimit + ") Zeichen!");
                                    return true;
                                }
                            }
                            String status = args[1];
                            savePlayerInStatus(p, status, "§f");
                            PrefixManager.updatePrefix(p);
                            p.sendMessage(Main.getInstance().getStatus_Prefix() +
                                    "§7Dein Status wurde auf §f" +
                                    ChatColor.translateAlternateColorCodes('&', status) +
                                    "§7 gesetzt!");
                        }
                        case 3 ->{
                            if (UppercaseLengthLimitToggle) {
                                if (args[1].length() >= Prefix_LengthLimit) {
                                    p.sendMessage(Main.getInstance().getStatus_Prefix() +
                                            "§7Dieser Status ist §9zu lang§7 (max " + Prefix_LengthLimit + ") Zeichen!");
                                    return true;
                                }
                            }
                            String status = args[1];
                            String arg2 = args[2].replace('&', '§');
                            String color = PrefixManager.getRawFromColor(arg2);
                            if (PrefixManager.isColorAColor(color)) {
                                savePlayerInStatus(p, status, color);
                                PrefixManager.updatePrefix(p);
                                p.sendMessage(Main.getInstance().getStatus_Prefix() +
                                        "§7Dein Status wurde auf " +
                                        color +
                                        ChatColor.translateAlternateColorCodes('&', status) +
                                        "§7 mit der Farbe " +
                                        color +
                                        PrefixManager.getColorFromRaw(color) +
                                        "§7 gesetzt!");
                            } else
                                p.sendMessage(Main.getInstance().getStatus_Prefix() +
                                        "§7Diese Farbe ist §9ungültig§7 und dein Status hat sich nicht geändert!");
                        }
                        case 4 ->{
                            if (p.hasPermission("status.admin")) {
                                String status = args[1];
                                String arg2 = args[2].replace('&', '§');
                                String color = PrefixManager.getRawFromColor(arg2);
                                if (!StringIsBlocked(args[1])) {
                                    if (UppercaseLengthLimitToggle) {
                                        if (args[1].length() >= Prefix_LengthLimit) {
                                            p.sendMessage(Main.getInstance().getStatus_Prefix() +
                                                    "§7Dieser Status ist §9zu lang§7 (max " + Prefix_LengthLimit + ") Zeichen!");
                                            return true;
                                        }
                                    }
                                    Player target = Bukkit.getPlayerExact(args[3]);
                                    if (PrefixManager.isColorAColor(color)) {
                                        savePlayerInStatus(target, status, color);
                                        PrefixManager.updatePrefix(target);
                                        for (Player all : Bukkit.getOnlinePlayers()) {
                                            all.setScoreboard(PrefixManager.getScoreboard());
                                        }
                                        p.sendMessage(Main.getInstance().getStatus_Prefix()
                                                + "§7Der Status von§9 "
                                                + target.getName()
                                                + "§7 wurde auf "
                                                + color
                                                + ChatColor.translateAlternateColorCodes('&', status)
                                                + "§7 mit der Farbe "
                                                + color
                                                + PrefixManager.getColorFromRaw(color)
                                                + "§7 gesetzt!"
                                        );
                                        target.sendMessage(Main.getInstance().getStatus_Prefix()
                                                + "§7 Der Spieler §9"
                                                + p.getName()
                                                + "§7 hat den Status von dir auf "
                                                + color
                                                + ChatColor.translateAlternateColorCodes('&', status)
                                                + "§7 mit der Farbe "
                                                + color
                                                + PrefixManager.getColorFromRaw(color)
                                                + "§7 gesetzt!"
                                        );
                                    } else
                                        p.sendMessage(Main.getInstance().getStatus_Prefix() +
                                                "§7Diese Farbe ist §9ungültig§7 und der Status von " +
                                                target.getName() + " hat sich nicht geändert!");
                                } else
                                    p.sendMessage(Main.getInstance().getStatus_Prefix() +
                                            "§9 Dieser Spieler wurde noch nicht registriert!");
                            } else p.sendMessage(Main.getInstance().getStatus_Prefix() +
                                    "§9 Du darfst dies nicht!");
                        }

                        default -> sendUsage(p);
                    }
                }
                case "get" -> {
                    switch (args.length){
                        case 1 -> {
                            String status = String.valueOf(statusData.get(p.getUniqueId() + ".status"));
                            String color = String.valueOf(statusData.get(p.getUniqueId() + ".color"));

                            if (playerIsRegistered(p)){
                                p.sendMessage(Main.getInstance().getStatus_Prefix() +
                                        "§7Dein Status ist " +
                                        color +
                                        ChatColor.translateAlternateColorCodes('&', status) +
                                        "§7 mit der Farbe " +
                                        color +
                                        PrefixManager.getColorFromRaw(color));
                            }
                        }
                        case 2 -> {
                            String t1 = args[1];
                            OfflinePlayer target = Bukkit.getOfflinePlayer(t1);
                            if (target.hasPlayedBefore()) {
                                String status = String.valueOf(statusData.get(target.getUniqueId() + ".status"));
                                String color = String.valueOf(statusData.get(target.getUniqueId() + ".color"));

                                if (PrefixManager.isColorAColor(color)) {
                                    p.sendMessage(Main.getInstance().getStatus_Prefix() +
                                            "§7Der Status von §9" +
                                            t1 +
                                            "§7 ist " +
                                            color +
                                            ChatColor.translateAlternateColorCodes('&', status) +
                                            "§7 mit der Farbe " +
                                            color +
                                            PrefixManager.getColorFromRaw(color));
                                } else {
                                    p.sendMessage(Main.getInstance().getStatus_Prefix() + "§7Der Spieler wurde nicht gefunden!");
                                }
                            } else {
                                p.sendMessage(Main.getInstance().getStatus_Prefix() + "§7Der Spieler wurde nicht gefunden!");
                            }
                        }
                        default -> sendUsage(p);
                    }
                }
            }
            return true;
        }
        System.out.println("You must be a player!");
        return true;
    }

    private void sendUsage(CommandSender sender) {
        if (sender.hasPermission("status.admin")) {
            sender.sendMessage("    §7Verwendung§8: §9/status <operator> <status> <color>          ");
            sender.sendMessage("    §7Verwendung§8: §9/status <operator> <status> <spieler>        ");
            sender.sendMessage("       §7<operator>§8: §7< §9\" help \"§7/§9\" set \"§7/§9\" get \"§7/§9\" reset \"§7/§9\" config \"§7>  ");
        } else {
            sender.sendMessage("§a|" + "    §7Verwendung§8: §9/status <operator> <status> <color>");
            sender.sendMessage("§a|" + "       §7<operator>§8: §7< §9\" help \"§7/§9\" set \"§7/§9\" get \"§7/§9\" reset \"§7>");
        }
    }

    private void sendUsage_Config(CommandSender sender) {
        sender.sendMessage("§a|§a" + "---------------------------------------------------" + "§a|§r");
        sender.sendMessage("§a|" + "    §7Verwendung§8: §9/config <operator>   " + "§a|§r");
        sender.sendMessage("§a|" + "    §7<operator>§8: §7< §9\" Coming soon \"§7>  " + "§a|§r");
        sender.sendMessage("§a|§a" + "---------------------------------------------------" + "§a|§r");
    }

    private void sendFailedCmd(CommandSender sender) {
        sender.sendMessage(Main.getInstance().getStatus_Prefix()+"§c§l Failed, Wrong Cmd or Token!");
    }
}