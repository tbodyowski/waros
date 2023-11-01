package de.tbodyowski.waros;


import de.tbodyowski.waros.AntiCheat.checks.CheckManager;
import de.tbodyowski.waros.AntiCheat.Data.DataManager;
import de.tbodyowski.waros.AntiCheat.Util.ReflectionUtils;
import de.tbodyowski.waros.Events.DeathEvent;
import de.tbodyowski.waros.Events.ElytraBoostEvent;
import de.tbodyowski.waros.Events.JoinQuitEvent;
import de.tbodyowski.waros.Recipes.Recipes;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;


public final class Main extends JavaPlugin {
    private static Main instance;
    private static NamespacedKey invisibleKey;
    private CheckManager checkManager;
    private DataManager dataManager;
    public boolean enabled;

    @Override
    public void onEnable() {
        checkManager = new CheckManager();
        dataManager = new DataManager();
        new ReflectionUtils();
        instance = this;
        invisibleKey = new NamespacedKey(this, "invisible");
        new Recipes().registerRecipe();
        System.out.println("WarOS is starting up...");
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(ElytraBoostEvent.create(this), this);
        getServer().getPluginManager().registerEvents(new DeathEvent(), this);
        getServer().getPluginManager().registerEvents(new JoinQuitEvent(), this);
        //getServer().getPluginManager().registerEvents(new InvisFrameListener(), this);
        enabled = true;
    }

    @Override
    public void onDisable() {
        enabled = false;
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
        System.out.println("WarOS is shutting down...");
        saveConfig();
    }

    public static Main getInstance() {
        return instance;
    }

    public static NamespacedKey getInvisibleKey() {
        return invisibleKey;
    }

    public CheckManager getCheckManager() {
        return checkManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
