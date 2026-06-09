package de.tbodyowski.pureos.manager;

import de.tbodyowski.pureos.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

public class PrefixManager {

    static String team = "001Spieler";
    private static Scoreboard defaultScoreboard;
    private static Scoreboard deathsScoreboard;

    private Team ensureTeam(Scoreboard scoreboard, String name) {
        Team existing = scoreboard.getTeam(name);
        return existing != null ? existing : scoreboard.registerNewTeam(name);
    }

    private String getSafePlayerTeamName(Player player) {
        // Team-Namen sind limitiert; UUID-basierter Name vermeidet Kollisionen mit Player#toString()
        String raw = "p" + player.getUniqueId().toString().replace("-", "");
        return raw.substring(0, 16);
    }

    public void setScoreboard() {
        defaultScoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        deathsScoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        ensureTeam(defaultScoreboard, team).setPrefix("");
        ensureTeam(deathsScoreboard, team).setPrefix("");
    }

    public static void updatePrefix(Player player) {
        if (defaultScoreboard == null || deathsScoreboard == null) {
            Main.getInstance().getPrefixManager().setScoreboard();
        }

        YamlConfiguration statusData = Main.getInstance().getFileManager().getStatusData();
        String playerTeam = Main.getInstance().getPrefixManager().getSafePlayerTeamName(player);

        if (player.isOnline()) {
            Team defaultPlayerTeam = Main.getInstance().getPrefixManager().ensureTeam(defaultScoreboard, playerTeam);
            Team deathsPlayerTeam = Main.getInstance().getPrefixManager().ensureTeam(deathsScoreboard, playerTeam);

            if (Objects.equals(statusData.getString(player.getUniqueId() + ".status"), "Default")) {
                defaultPlayerTeam.setPrefix("");

                deathsPlayerTeam.setPrefix("§f[" + player.getStatistic(Statistic.DEATHS) + "§f] ");
            } else {
                defaultPlayerTeam.setPrefix("§f[" + statusData.getString(player.getUniqueId() + ".color")
                        + ChatColor.translateAlternateColorCodes('&', (statusData.getString(player.getUniqueId() + ".status")) + "§f] §f"));

                deathsPlayerTeam.setPrefix("§f[" + player.getStatistic(Statistic.DEATHS) + "§f] "
                        + "§f[" + statusData.getString(player.getUniqueId() + ".color")
                        + ChatColor.translateAlternateColorCodes('&', (statusData.getString(player.getUniqueId() + ".status")) + "§f] §f"));

            }

            if (statusData.getBoolean(player.getUniqueId() + ".Afk")){
                defaultPlayerTeam.setSuffix("§r §c[" + "AFK" + "]§r");
                deathsPlayerTeam.setSuffix("§r §c[" + "AFK" + "]§r");
            } else {
                defaultPlayerTeam.setSuffix("");
                deathsPlayerTeam.setSuffix("");
            }

            defaultPlayerTeam.addEntry(player.getName());
            deathsPlayerTeam.addEntry(player.getName());



            Main.getInstance().getFileManager().saveStatusFile();
        }
    }

    public String getTeamByPlayer(Player player) {
        return getSafePlayerTeamName(player);
    }

    public void updatePrefixAllPlayers() {
        setScoreboard();

        for (Player target : Bukkit.getOnlinePlayers()) {
            Main.getInstance().getFileManager().saveStatusFile();
            Main.getInstance().getPrefixManager().updatePrefix(target);
            target.setScoreboard(Main.getInstance().getPrefixManager().getScoreboard(target));
        }
    }

    public static Boolean isColorAColor(String Color) {
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

    public String getColorFromRaw(String raw) {
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

    public String getRawFromColor(String Color) {
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

    public Scoreboard getScoreboard(Player player) {
        YamlConfiguration statusData = Main.getInstance().getFileManager().getStatusData();

        return Main.getInstance().getConfigVarManager().getDeathCounter_on_off() && statusData.getBoolean(player.getUniqueId() + ".p-settings" + ".DeathCounter_on_off")
                ? deathsScoreboard : defaultScoreboard;
    }

    public Scoreboard getDefaultScoreboard() {
        return defaultScoreboard;
    }

    public Scoreboard getDeathsScoreboard() {
        return deathsScoreboard;
    }

    public String getTeam() {
        return team;
    }

    /**
     * Bereinigt alle Scoreboards und Spieler-Teams beim Plugin-Disable
     */
    public void cleanup() {
        try {
            // Alle Spieler zurücksetzen auf das Standard-Scoreboard
            for (Player player : Bukkit.getOnlinePlayers()) {
                Scoreboard emptyScoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
                player.setScoreboard(emptyScoreboard);
            }
            
            // Alle Teams von den Scoreboards löschen
            if (defaultScoreboard != null) {
                for (org.bukkit.scoreboard.Team teams : defaultScoreboard.getTeams()) {
                    teams.unregister();
                }
            }
            
            if (deathsScoreboard != null) {
                for (org.bukkit.scoreboard.Team teams : deathsScoreboard.getTeams()) {
                    teams.unregister();
                }
            }
            
            // Scoreboards auf null setzen
            defaultScoreboard = null;
            deathsScoreboard = null;
        } catch (Exception e) {
            if (Main.getInstance() != null) {
                Main.getInstance().getLogger().warning("Fehler beim Cleanup der Scoreboards: " + e.getMessage());
            }
        }
    }
}