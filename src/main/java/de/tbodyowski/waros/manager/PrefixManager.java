package de.tbodyowski.waros.manager;

import de.tbodyowski.waros.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Objects;


public class PrefixManager {

    static String team = "001Spieler";
    private static Scoreboard scoreboard;

    public static void setScoreboard() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        scoreboard.registerNewTeam(team);

        scoreboard.getTeam(team).setPrefix("§f[Spieler] §f");
    }

    public static void updatePrefix(Player player) {
        YamlConfiguration statusData = Main.getInstance().getFileManager().getStatusData();
        String playerTeam = "001"+player;

        if (player.isOnline()) {
            try {
                scoreboard.registerNewTeam(playerTeam);
            } catch (Exception ignored) {}

            if (Main.getInstance().getDeathCounter_on_off()) {
                if (Objects.equals(statusData.getString(player.getUniqueId() + ".status"), "Default")) {
                    if (player.getStatistic(Statistic.DEATHS)>0) {
                        scoreboard.getTeam(playerTeam).setPrefix("§f[" + player.getStatistic(Statistic.DEATHS) + "§f]" + " §f[" + "Spieler" + "§f] §f");
                    }else{
                        scoreboard.getTeam(playerTeam).setPrefix("§f[" + "Spieler" + "§f] §f");
                    }
                } else {
                    if (player.getStatistic(Statistic.DEATHS)>0) {
                        scoreboard.getTeam(playerTeam).setPrefix("§f[" + player.getStatistic(Statistic.DEATHS) + "§f]"
                                + " §f[" + statusData.getString(player.getUniqueId() + ".color")
                                + ChatColor.translateAlternateColorCodes('&', statusData.getString(player.getUniqueId() + ".status")) + "§f] §f");
                    }else{
                        scoreboard.getTeam(playerTeam).setPrefix("§f[" + statusData.getString(player.getUniqueId() + ".color")
                                + ChatColor.translateAlternateColorCodes('&', statusData.getString(player.getUniqueId() + ".status")) + "§f] §f");
                    }
                }
            } else {
                if (Objects.equals(statusData.getString(player.getUniqueId() + ".status"), "Default")) {
                    scoreboard.getTeam(playerTeam).setPrefix("§f[" + "Spieler" + "§f] §f");
                } else {
                    scoreboard.getTeam(playerTeam).setPrefix("§f[" + statusData.getString(player.getUniqueId() + ".color")
                            + ChatColor.translateAlternateColorCodes('&', (statusData.getString(player.getUniqueId() + ".status")) + "§f] §f"));
                }
            }

            scoreboard.getTeam(playerTeam).addEntry(player.getDisplayName());

            player.setScoreboard(scoreboard);
        }
    }

    public static void updatePrefixAllPlayers(){
        YamlConfiguration statusData = Main.getInstance().getFileManager().getStatusData();

        PrefixManager.setScoreboard();

        for (Player all : Bukkit.getOnlinePlayers()){
            if (Objects.equals(statusData.getString(all.getUniqueId() + ".status"), "Default")){
                PrefixManager.getScoreboard().getTeam(team).addEntry(all.getDisplayName());
            }else {
                PrefixManager.updatePrefix(all);
            }
        }

        for (Player all : Bukkit.getOnlinePlayers()){
            all.setScoreboard(PrefixManager.getScoreboard());
        }
    }

    public static String getTeamByPlayer(Player player) {
        return "001" + player;
    }

    public static Boolean isColorAColor(String Color){
        return switch (Color.toLowerCase()) {
            case "§" + "0",
                 "§" + "1",
                 "§" + "2",
                 "§" + "3",
                 "§" + "4",
                 "§" + "5",
                 "§" + "6",
                 "§" + "7",
                 "§" + "8",
                 "§" + "9",
                 "§" + "a",
                 "§" + "b",
                 "§" + "c",
                 "§" + "d",
                 "§" + "e",
                 "§" + "f",
                 "black",
                 "dark_blue",
                 "dark_green",
                 "dark_aqua",
                 "dark_red",
                 "dark_purple",
                 "gold",
                 "gray",
                 "dark_gray",
                 "blue",
                 "green",
                 "aqua",
                 "red",
                 "light_purple",
                 "yellow",
                 "white" -> true;
            default -> false;
        };
    }

    public static String getColorFromRaw(String raw){
        return switch (raw.toLowerCase()) {
            case "§0" -> "Black";
            case "§1" -> "Dark Blue";
            case "§2" -> "Dark Green";
            case "§3" -> "Dark Aqua";
            case "§4" -> "Dark Red";
            case "§5" -> "Dark Purple";
            case "§6" -> "Gold";
            case "§7" -> "Gray";
            case "§8" -> "Dark Gray";
            case "§9" -> "Blue";
            case "§a" -> "Green";
            case "§b" -> "Aqua";
            case "§c" -> "Red";
            case "§d" -> "Light Purple";
            case "§e" -> "Yellow";
            case "§f" -> "White";
            default -> "default";
        };
    }

    public static String getRawFromColor(String Color){
        return switch (Color.toLowerCase()) {
            case "black" -> "§0";
            case "dark_blue" -> "§1";
            case "dark_green" -> "§2";
            case "dark_aqua" -> "§3";
            case "dark_red" -> "§4";
            case "dark_purple" -> "§5";
            case "gold" -> "§6";
            case "gray" -> "§7";
            case "dark_gray" -> "§8";
            case "blue" -> "§9";
            case "green" -> "§a";
            case "aqua" -> "§b";
            case "red" -> "§c";
            case "light_purple" -> "§d";
            case "yellow" -> "§e";
            case "white" -> "§f";
            default -> Color;
        };
    }

    public static Scoreboard getScoreboard() {
        return scoreboard;
    }

    public static String getTeam() {
        return team;
    }
}
