package de.tbodyowski.pureos;

import de.tbodyowski.pureos.Events.*;
import de.tbodyowski.pureos.commands.*;
import de.tbodyowski.pureos.commands.config.ConfigCommand;
import de.tbodyowski.pureos.inventory.GhastSpeedInventory;
import de.tbodyowski.pureos.manager.*;
import de.tbodyowski.pureos.recipe.CharCoalToCoalBlock;
import de.tbodyowski.pureos.recipe.CharcoalToBlackDye;
import de.tbodyowski.pureos.recipe.CoalToBlackDye;
import de.tbodyowski.pureos.recipe.LightBlockRecipe;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;


public final class Main extends JavaPlugin implements Listener {

    private String Status_Prefix = "";
    private Boolean DeathCounter_on_off = (Boolean) false;
    private static Main instance;
    private PrefixManager prefixManager;
    private FileManager fileManager;
    private GhastSpeedInventory ghastSpeedInventory;
    private InventorySeeManager inventorySeeManager;

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
        this.inventorySeeManager = new InventorySeeManager(this);
        configVarManager.updateVar();

        new LightBlockRecipe(this).register();
        new CoalToBlackDye(this).register();
        new CharcoalToBlackDye(this).register();
        new CharCoalToCoalBlock(this).register();

        reload();
        getLogger().info("PureOS is starting up...");

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(ElytraBoostEvent.create(this), this);
        getServer().getPluginManager().registerEvents(new DeathEvent(), this);
        getServer().getPluginManager().registerEvents(new SitEvent(), this);
        getServer().getPluginManager().registerEvents(new HorseRideSpeedEvent(this), this);
        getServer().getPluginManager().registerEvents(new GhastSpeedInventory(), this);
        getServer().getPluginManager().registerEvents(new LightBlockBreakEvent(), this);
        if (this.getConfig().getBoolean("Status-Prefix-on/off")) {
            this.Status_Prefix = this.getConfig().getString("Status-Prefix");
        }
        this.DeathCounter_on_off = (Boolean) this.getConfig().getBoolean("DeathCounter-on/off");

        Bukkit.getPluginManager().registerEvents(new EventManager(this),this);
        Bukkit.getPluginManager().registerEvents(this.inventorySeeManager, this);
        getCommand("status").setExecutor(new StatusCommand());
        getCommand("status").setTabCompleter(new StatusTabComplete());
        getCommand("vanish").setExecutor(new VanishCommand());
        getCommand("sit").setExecutor(new SitCommand());
        getCommand("ghastspeed").setExecutor(new GhastSpeedCommand());
        getCommand("config").setExecutor(new ConfigCommand());
        getCommand("config").setTabCompleter(new ConfigCommand());
        getCommand("adminchat").setExecutor(new AdminChatCommand(this));
        InvseeCommand invseeCommand = new InvseeCommand(this.inventorySeeManager);
        Objects.requireNonNull(getCommand("invsee")).setExecutor(invseeCommand);
        Objects.requireNonNull(getCommand("invsee")).setTabCompleter(invseeCommand);
        EndseeCommand endseeCommand = new EndseeCommand(this.inventorySeeManager);
        Objects.requireNonNull(getCommand("endsee")).setExecutor(endseeCommand);
        Objects.requireNonNull(getCommand("endsee")).setTabCompleter(endseeCommand);

        getPrefixManager().setScoreboard();
        startSaveAndRegisterPlayer();
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

    }

    @Override
    public void onDisable() {
        getLogger().info("WarOS is shutting down...");
        
        // Cleanup Scoreboards und Player-Daten
        if (this.prefixManager != null) {
            this.prefixManager.cleanup();
            getLogger().info("Scoreboards cleaned up.");
        }
        
        // Speichere Config und Dateien
        saveConfig();
        if (this.getFileManager() != null) {
            this.getFileManager().saveStatusFile();
            this.getFileManager().saveBlockedWordsFile();
        } else {
            getLogger().warning("FileManager is null during shutdown. Skipping file save.");
        }

        if (this.inventorySeeManager != null) {
            this.inventorySeeManager.shutdown();
        }
        
        // Setze statische Instanz auf null
        instance = null;
        
        // Setze Manager auf null
        this.prefixManager = null;
        this.fileManager = null;
        this.configVarManager = null;
        this.ghastSpeedInventory = null;
        this.inventorySeeManager = null;
        
        getLogger().info("WarOS shutdown complete.");
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
            if (statusData.getString(all.getUniqueId() + ".status") == null) {
                Main.getInstance().getFileManager().savePlayerInStatus(all, "Default", "§f");
            }
            Main.getInstance().getPrefixManager().updatePrefix(all);
            all.setScoreboard(Main.getInstance().getPrefixManager().getScoreboard(all));
        }
    }
    public ConfigVarManager getConfigVarManager() {
        return configVarManager;
    }

    public PrefixManager getPrefixManager() {
        return prefixManager;
    }

    public InventorySeeManager getInventorySeeManager() {
        return inventorySeeManager;
    }
}