package de.tbodyowski.pureos;

import de.tbodyowski.pureos.Events.*;
import de.tbodyowski.pureos.commands.*;
import de.tbodyowski.pureos.inventory.GhastSpeedInventory;
import de.tbodyowski.pureos.manager.*;
import de.tbodyowski.pureos.util.DroppedFrameLocation;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public final class Main extends JavaPlugin implements Listener {

    private String Status_Prefix = "";
    private Boolean DeathCounter_on_off = (Boolean) false;
    private static Main instance;
    private PrefixManager prefixManager;
    private FileManager fileManager;
    private GhastSpeedInventory ghastSpeedInventory;

    private ConfigVarManager configVarManager;
    File configFile = new File(getDataFolder(), "config.yml");



    @Override
    public void onEnable() {
        if (!configFile.exists()) saveDefaultConfig();
        instance = this;
        this.prefixManager = new PrefixManager();
        this.configVarManager = new ConfigVarManager();
        this.fileManager = new FileManager();
        this.ghastSpeedInventory = new GhastSpeedInventory();
        configVarManager.updateVar();

        reload();
        getLogger().info("WarOS is starting up...");

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(ElytraBoostEvent.create(this), this);
        getServer().getPluginManager().registerEvents(new DeathEvent(), this);
        getServer().getPluginManager().registerEvents(new SitEvent(), this);
        getServer().getPluginManager().registerEvents(new GhastSpeedInventory(), this);
        if (this.getConfig().getBoolean("Status-Prefix-on/off")) {
            this.Status_Prefix = this.getConfig().getString("Status-Prefix");
        }
        this.DeathCounter_on_off = (Boolean) this.getConfig().getBoolean("DeathCounter-on/off");

        Bukkit.getPluginManager().registerEvents(new EventManager(),this);
        getCommand("status").setExecutor(new StatusCommand());
        getCommand("status").setTabCompleter(new StatusTabComplete());
        getCommand("vanish").setExecutor(new VanishCommand());
        getCommand("sit").setExecutor(new SitCommand());
        getCommand("ghastspeed").setExecutor(new GhastSpeedCommand());

        getPrefixManager().setScoreboard();
        startSaveAndRegisterPlayer();
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

    }

    @Override
    public void onDisable() {
        getLogger().info("WarOS is shutting down...");
        saveConfig();
        if (this.getFileManager() != null) {
            this.getFileManager().saveStatusFile();
            this.getFileManager().saveBlockedWordsFile();
        } else {
            getLogger().warning("FileManager is null during shutdown. Skipping file save.");
        }
    }

    public static Main getInstance() {
        return instance;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public GhastSpeedInventory getGhastSpeedInventory() {
        return ghastSpeedInventory;
    }

    public String getStatus_Prefix() {
        return Status_Prefix;
    }

    public void reload() {
        saveConfig();
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }
    public static void startSaveAndRegisterPlayer() {
        YamlConfiguration statusData = Main.getInstance().getFileManager().getStatusData();
        for (Player all : Bukkit.getOnlinePlayers()) {
            all.setScoreboard(Main.getInstance().getPrefixManager().getScoreboard(all));
        }
        for (Player all : Bukkit.getOnlinePlayers()) {
            if (statusData.getString(all.getUniqueId().toString()) == null) {
                Main.getInstance().getFileManager().savePlayerInStatus(all, "Default", "§f");
                Main.getInstance().getPrefixManager().getScoreboard(all).getTeam(Main.getInstance().getPrefixManager().getTeam()).addEntry(all.getDisplayName());
            }
            if (statusData.getString(all.getUniqueId() + ".status").equals("Default")) {
                Main.getInstance().getPrefixManager().getScoreboard(all).getTeam(Main.getInstance().getPrefixManager().getTeam()).addEntry(all.getDisplayName());
            } else {
                Main.getInstance().getPrefixManager().updatePrefix(all);
            }
        }
    }
    public ConfigVarManager getConfigVarManager() {
        return configVarManager;
    }

    public PrefixManager getPrefixManager() {
        return prefixManager;
    }
}