package de.tbodyowski.pureos.manager;

import de.tbodyowski.pureos.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class FileManager {

    private final File statusDataFile;
    private YamlConfiguration statusData;

    private final File blockedWordsDataFile;
    private YamlConfiguration blockedWordsData;

    private final File guildDataFile;
    private YamlConfiguration guildData;

    private final File chestDataFile;
    private YamlConfiguration chestData;


    public FileManager(){
        File folder = new File("./plugins/PureOS/");
        this.statusDataFile = new File(folder, "status.yml");
        this.blockedWordsDataFile = new File(folder, "blockedWords.yml");
        this.guildDataFile = new File(folder, "guilds.yml");
        this.chestDataFile = new File(folder, "chests.yml");


        try {
            if (!folder.exists()) folder.mkdirs();
            if (!statusDataFile.exists()) statusDataFile.createNewFile();
            statusData = YamlConfiguration.loadConfiguration(statusDataFile);

            if (!blockedWordsDataFile.exists()) blockedWordsDataFile.createNewFile();
            blockedWordsData = YamlConfiguration.loadConfiguration(blockedWordsDataFile);

            if (!guildDataFile.exists()) guildDataFile.createNewFile();
            guildData = YamlConfiguration.loadConfiguration(guildDataFile);

            if (!chestDataFile.exists()) chestDataFile.createNewFile();
            chestData = YamlConfiguration.loadConfiguration(chestDataFile);


        } catch (IOException e) {
            logIoError("Fehler beim Initialisieren der Dateiverwaltung", e);
        }
    }

    public YamlConfiguration getStatusData() {
        reloadStatusFile();
        return statusData;
    }

    public void reloadStatusFile() {
        statusData = YamlConfiguration.loadConfiguration(statusDataFile);
    }
    public void reloadChestFile() {
        chestData = YamlConfiguration.loadConfiguration(chestDataFile);
    }

    public void reloadBlockedWordsFile() {
        blockedWordsData = YamlConfiguration.loadConfiguration(blockedWordsDataFile);
    }

    public void saveStatusFile() {
        try {
            statusData.save(statusDataFile);
        } catch (IOException e) {
            logIoError("Fehler beim Speichern von status.yml", e);
        }
        reloadStatusFile();
    }
    public void saveBlockedWordsFile() {
        try {
            blockedWordsData.save(blockedWordsDataFile);
        } catch (IOException e) {
            logIoError("Fehler beim Speichern von blockedWords.yml", e);
        }
    }

    private void logIoError(String message, IOException exception) {
        if (Main.getInstance() != null) {
            Main.getInstance().getLogger().warning(message + ": " + exception.getMessage());
            return;
        }
        java.util.logging.Logger.getLogger("PureOS").warning(message + ": " + exception.getMessage());
    }

    public YamlConfiguration getBlockedWordsData() {
        reloadBlockedWordsFile();
        return blockedWordsData;
    }

    public static boolean playerIsRegistered(Player player) {
        YamlConfiguration statusData = Main.getInstance().getFileManager().getStatusData();
        return statusData.contains(player.getUniqueId().toString());
    }

    public void savePlayerInStatus(Player player, String status, String color) {
        if (playerIsRegistered(player)){
            statusData.set(player.getUniqueId() + ".player", player.getName());
            statusData.set(player.getUniqueId() + ".status", status);
            statusData.set(player.getUniqueId() + ".color", color);
        }else{
            savePlayerInStatusWithPersonalWithAfk(player, status, color, true, true, true, true, false);
        }
    }
    public void savePlayerInStatusWithPersonalWithAfk(Player player, String status, String color,
                                                      Boolean Status_Prefix_on_off,
                                                      Boolean Join_Leave_Message_on_off,
                                                      Boolean DeathCounter_on_off,
                                                      Boolean AutoAfk_on_off,
                                                      Boolean Afk) {

        int value = 1;

        if (statusData.contains(player.getUniqueId().toString())) {
            value = Objects.requireNonNull(statusData.getConfigurationSection(player.getUniqueId().toString())).getKeys(false).size() + 1;
        }

        statusData.set(player.getUniqueId() + ".player", player.getName());
        statusData.set(player.getUniqueId() + ".status", status);
        statusData.set(player.getUniqueId() + ".color", color);
        statusData.set(player.getUniqueId() + ".Afk", Afk);

        //personal Settings
        ConfigurationSection playerSettings = statusData.createSection(player.getUniqueId()+".p-settings");
        playerSettings.set(".Status_Prefix_on_off",Status_Prefix_on_off);
        playerSettings.set(".Join_Leave_Message_on_off",Join_Leave_Message_on_off);
        playerSettings.set(".DeathCounter_on_off",DeathCounter_on_off);
        playerSettings.set(".AutoAfk_on_off",AutoAfk_on_off);

        Main.getInstance().getFileManager().saveStatusFile();
    }

    public static boolean StringIsBlocked(String message) {
        YamlConfiguration blockedWordsData = Main.getInstance().getFileManager().getBlockedWordsData();
        List<String> blockedWords = blockedWordsData.getStringList("blockedWords");
        for (String word : blockedWords) {
            if (message.contains(word)) {
                return true;
            }
        }
        return false;
    }
}