package de.tbodyowski.waros.manager;

import de.tbodyowski.waros.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.List;

public class FileManager {

    private final File statusDataFile;
    private YamlConfiguration statusData;

    private final File blockedWordsDataFile;
    private YamlConfiguration blockedWordsData;

    public FileManager(){
        File folder = new File("./plugins/WarOS/");
        this.statusDataFile = new File(folder, "status.yml");

        File folder2 = new File("./plugins/WarOS/");
        this.blockedWordsDataFile = new File(folder2, "blockedWords.yml");

        try{
            if (!folder.exists()) folder.mkdirs();
            if (!statusDataFile.exists()) statusDataFile.createNewFile();
            statusData = YamlConfiguration.loadConfiguration(statusDataFile);

            if (!folder2.exists()) folder2.mkdirs();
            if (!blockedWordsDataFile.exists()) blockedWordsDataFile.createNewFile();
            blockedWordsData = YamlConfiguration.loadConfiguration(blockedWordsDataFile);

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void reloadStatusFile(){
        YamlConfiguration.loadConfiguration(statusDataFile);
    }

    public void reloadBlockedWordsFile(){
        YamlConfiguration.loadConfiguration(blockedWordsDataFile);
    }

    public void saveStatusFile(){
        try {
            statusData.save(statusDataFile);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void saveBlockedWordsFile(){
        try {
            blockedWordsData.save(blockedWordsDataFile);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public YamlConfiguration getStatusData() {
        reloadStatusFile();
        return statusData;
    }

    public YamlConfiguration getBlockedWordsData() {
        reloadBlockedWordsFile();
        return blockedWordsData;
    }

    public static void savePlayerInStatus(Player player, String status, String color){
        YamlConfiguration statusData = Main.getInstance().getFileManager().getStatusData();
        int value = 1;
        Boolean afk = false;

        if (statusData.contains(player.getUniqueId().toString())){
            value = statusData.getConfigurationSection(player.getUniqueId().toString()).getKeys(false).size() + 1;
            afk = false;
        }

        statusData.set(player.getUniqueId() + ".player", player.getName());
        statusData.set(player.getUniqueId() + ".status", status);
        statusData.set(player.getUniqueId() + ".color", color);
        statusData.set(player.getUniqueId() + ".afk" , afk);

        Main.getInstance().getFileManager().saveStatusFile();

    }
    public static Boolean playerIsRegistered(Player player){
        YamlConfiguration statusData = Main.getInstance().getFileManager().getStatusData();
        return statusData.getString(player.getUniqueId().toString()) != null;
    }

    public static Boolean StringIsBlocked(String string){
        if (Main.getInstance().getConfig().getBoolean("BlockedWords-Toggle")) {
            YamlConfiguration blockedWordsData = Main.getInstance().getFileManager().getBlockedWordsData();
            String[] words = string.split(" ");
            List<String> customWords = blockedWordsData.getStringList("cm-baned-words");
            List<String> blockedWords = blockedWordsData.getStringList("banned-words");
            List<String> blockedWords_enl = blockedWordsData.getStringList("banned-words-enl");
            List<String> blockedWords_gl_enl = blockedWordsData.getStringList("banned-words-gl-enl");

            for (String word : words) {
                for (String bWords : customWords) {
                    if (word.equalsIgnoreCase(bWords)) {
                        return true;
                    }
                }
            }

            for (String word : words) {
                for (String bWords : blockedWords) {
                    if (word.equalsIgnoreCase(bWords)) {
                        return true;
                    }
                }
            }

            for (String word : words) {
                for (String bWords : blockedWords_enl) {
                    if (word.equalsIgnoreCase(bWords)) {
                        return true;
                    }
                }
            }

            for (String word : words) {
                for (String bWords : blockedWords_gl_enl) {
                    if (word.equalsIgnoreCase(bWords)) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
