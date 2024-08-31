package de.tbodyowski.waros.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class StatusTabComplete implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        /*TODO**
          Argumente
         /status 1 2 3 4
         */

        ArrayList<String> list = new ArrayList<>();
        if (args.length == 0) return list;
        if (args.length == 1) {
            list.add("help");
            list.add("set");
            list.add("reset");
            list.add("get");
        }
        if (args.length == 2) {
            String operator = args[1].toLowerCase();
            switch (operator) {
                case "reset":
                    if (sender.hasPermission("status.admin")) {
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            list.add(online.getName());
                        }
                    }
                    break;
                case "get":
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        list.add(online.getName());
                    }
                    break;
                default:
                    return list;
            }
        }
        if (args.length == 3) {
            for (int i = 1; i < 10; i++) {
                list.add("&" + i);
            }
            for (char c = 'a'; c <= 'f'; c++) {
                list.add("&" + c);
            }

            list.add("black");
            list.add("dark_blue");
            list.add("dark_green");
            list.add("dark_aqua");
            list.add("dark_red");
            list.add("dark_purple");
            list.add("gold");
            list.add("gray");
            list.add("dark_gray");
            list.add("blue");
            list.add("green");
            list.add("aqua");
            list.add("red");
            list.add("light_purple");
            list.add("yellow");
            list.add("white");
        }

        if (args.length == 4 && sender.hasPermission("status.admin")) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                list.add(online.getName());
            }
        }

        ArrayList<String> completerList = new ArrayList<>();
        String currentArg = args[args.length-1].toLowerCase();
        for (String s : list){
            String s1 = s.toLowerCase();
            if (s1.startsWith(currentArg)){
                completerList.add(s);
            }
        }
        return list;
    }
}
