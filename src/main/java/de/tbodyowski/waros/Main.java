package de.tbodyowski.waros;

import de.tbodyowski.waros.Events.DeathEvent;
import de.tbodyowski.waros.Events.ElytraBoostEvent;
import de.tbodyowski.waros.commands.StatusCommand;
import de.tbodyowski.waros.commands.StatusTabComplete;
import de.tbodyowski.waros.manager.EventManager;
import de.tbodyowski.waros.manager.FileManager;
import de.tbodyowski.waros.manager.PrefixManager;
import de.tbodyowski.waros.util.DroppedFrameLocation;

import lombok.Getter;
import org.bukkit.*;
import org.bukkit.command.Command;
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


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;


public final class Main extends JavaPlugin implements Listener {

    private static final String REPO_OWNER = "tbodyowski";
    private static final String REPO_NAME = "waros";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/releases/latest";
    private static final String GITHUB_TOKEN = "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";  // Your GitHub token here
    private static final String VERSION_FILE = "latest_version.txt";

    private String Status_Prefix = "";
    @Getter
    private Boolean DeathCounter_on_off = false;
    private static Main instance;
    private NamespacedKey invisibleRecipe;
    private static NamespacedKey invisibleKey;
    private Set<DroppedFrameLocation> droppedFrames;
    private boolean framesGlow;
    private boolean firstLoad = true;
    private PrefixManager prefixManager;
    private FileManager fileManager;
    private Material glowInkSac = null;
    private Material glowFrame = null;
    private EntityType glowFrameEntity = null;


    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        this.prefixManager = new PrefixManager();
        this.fileManager = new FileManager();
        invisibleRecipe = new NamespacedKey(this, "invisible-recipe");
        invisibleKey = new NamespacedKey(this, "invisible");
        droppedFrames = new HashSet<>();
        try {
            glowInkSac = Material.valueOf("GLOW_INK_SAC");
            glowFrame = Material.valueOf("GLOW_ITEM_FRAME");
            glowFrameEntity = EntityType.valueOf("GLOW_ITEM_FRAME");
        } catch (IllegalArgumentException ignored) {
        }
        reload();
        getLogger().info("WarOS is starting up...");

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(ElytraBoostEvent.create(this), this);
        getServer().getPluginManager().registerEvents(new DeathEvent(), this);

        if (this.getConfig().getBoolean("Status-Prefix-on/off")) {
            this.Status_Prefix = this.getConfig().getString("Status-Prefix");
        }
        this.DeathCounter_on_off = this.getConfig().getBoolean("DeathCounter-on/off");

        Bukkit.getPluginManager().registerEvents(new EventManager(this),this);
        getCommand("status").setExecutor(new StatusCommand());
        getCommand("status").setTabCompleter(new StatusTabComplete());
        PrefixManager.setScoreboard();
        startSaveAndRegisterPlayer();
    }

    @Override
    public void onDisable() {
        getLogger().info("WarOS is shutting down...");
        saveConfig();
        removeRecipe();
        if (this.getFileManager() != null) {
            this.getFileManager().saveStatusFile();
            this.getFileManager().saveBlockedWordsFile();
        } else {
            getLogger().warning("FileManager is null during shutdown. Skipping file save.");
        }
        saveDefaultConfig();
    }

    private void removeRecipe() {
        Iterator<Recipe> iter = getServer().recipeIterator();
        while (iter.hasNext()) {
            Recipe check = iter.next();
            if (isInvisibleRecipe(check)) {
                iter.remove();
                break;
            }
        }

    }

    public static Main getInstance() {
        return instance;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public String getStatus_Prefix() {
        return Status_Prefix;
    }

    public void setRecipeItem(ItemStack item) {
        getConfig().set("recipe", item);
        saveConfig();
        reload();
    }

    public void reload() {
        saveDefaultConfig();
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        removeRecipe();

        if (firstLoad) {
            firstLoad = false;
            framesGlow = !getConfig().getBoolean("item-frames-glow");
        }
        if (getConfig().getBoolean("item-frames-glow") != framesGlow) {
            framesGlow = getConfig().getBoolean("item-frames-glow");
            forceRecheck();
        }

        ItemStack invisibleItem = generateInvisibleItemFrame();
        invisibleItem.setAmount(8);

        ItemStack invisibilityPotion = getConfig().getItemStack("recipe");
        ShapedRecipe invisRecipe = new ShapedRecipe(invisibleRecipe, invisibleItem);
        invisRecipe.shape("FFF", "FPF", "FFF");
        invisRecipe.setIngredient('F', Material.ITEM_FRAME);
        invisRecipe.setIngredient('P', Material.GLOWSTONE_DUST);
        Bukkit.addRecipe(invisRecipe);
    }

    public void forceRecheck() {
        for (World world : Bukkit.getWorlds()) {
            for (ItemFrame frame : world.getEntitiesByClass(ItemFrame.class)) {
                if (frame.getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE)) {
                    if (frame.getItem().getType() == Material.AIR && framesGlow) {
                        frame.setGlowing(true);
                        frame.setVisible(true);
                    } else if (frame.getItem().getType() != Material.AIR) {
                        frame.setGlowing(false);
                        frame.setVisible(false);
                    }
                }
            }
        }
    }

    private boolean isInvisibleRecipe(Recipe recipe) {
        return (recipe instanceof ShapedRecipe && ((ShapedRecipe) recipe).getKey().equals(invisibleRecipe));
    }

    private boolean isFrameEntity(Entity entity) {
        return (entity != null && (entity.getType() == EntityType.ITEM_FRAME ||
                (glowFrameEntity != null && entity.getType() == glowFrameEntity)));
    }

    public static ItemStack generateInvisibleItemFrame() {
        ItemStack item = new ItemStack(Material.ITEM_FRAME, 1);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.setDisplayName(ChatColor.WHITE + "Invisible Item Frame");
        meta.getPersistentDataContainer().set(invisibleKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler(ignoreCancelled = true)
    private void onCraft(PrepareItemCraftEvent event) {
        if (isInvisibleRecipe(event.getRecipe()) && !event.getView().getPlayer().hasPermission("survivalinvisiframes.craft")) {
            event.getInventory().setResult(null);
        } else if (glowInkSac != null && glowFrame != null) {
            boolean foundFrame = false;
            boolean foundInkSac = false;
            for (ItemStack i : event.getInventory().getMatrix()) {
                if (i == null || i.getType() == Material.AIR) continue;

                if (i.getType() == glowInkSac) {
                    if (foundInkSac) return;
                    foundInkSac = true;
                    continue;
                }

                if (i.getItemMeta().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE) &&
                        i.getType() != glowFrame) {
                    if (foundFrame) return;
                    foundFrame = true;
                    continue;
                }

                // Item isn't what we're looking for
                return;
            }

            if (foundFrame && foundInkSac && event.getView().getPlayer().hasPermission("survivalinvisiframes.craft")) {
                ItemStack invisibleGlowingItem = generateInvisibleItemFrame();
                ItemMeta meta = invisibleGlowingItem.getItemMeta();
                meta.setDisplayName(ChatColor.WHITE + "Glow Invisible Item Frame");
                invisibleGlowingItem.setItemMeta(meta);
                invisibleGlowingItem.setType(glowFrame);

                event.getInventory().setResult(invisibleGlowingItem);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onHangingPlace(HangingPlaceEvent event) {
        if (!isFrameEntity(event.getEntity()) || event.getPlayer() == null) {
            return;
        }

        // Get the frame item that the player placed
        ItemStack frame;
        Player p = event.getPlayer();
        if (p.getInventory().getItemInMainHand().getType() == Material.ITEM_FRAME ||
                (glowFrame != null && p.getInventory().getItemInMainHand().getType() == glowFrame)) {
            frame = p.getInventory().getItemInMainHand();
        } else if (p.getInventory().getItemInOffHand().getType() == Material.ITEM_FRAME ||
                (glowFrame != null && p.getInventory().getItemInOffHand().getType() == glowFrame)) {
            frame = p.getInventory().getItemInOffHand();
        } else {
            return;
        }

        // If the frame item has the invisible tag, make the placed item frame invisible
        if (frame.getItemMeta().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE)) {
            if (!p.hasPermission("survivalinvisiframes.place")) {
                event.setCancelled(true);
                return;
            }
            ItemFrame itemFrame = (ItemFrame) event.getEntity();
            if (framesGlow) {
                itemFrame.setVisible(true);
                itemFrame.setGlowing(true);
            } else {
                itemFrame.setVisible(false);
            }
            event.getEntity().getPersistentDataContainer().set(invisibleKey, PersistentDataType.BYTE, (byte) 1);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onHangingBreak(HangingBreakEvent event) {
        if (!isFrameEntity(event.getEntity()) || !event.getEntity().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE)) {
            return;
        }

        // This is the dumbest possible way to change the drops of an item frame
        // Apparently, there's no api to change the dropped item
        // So this sets up a bounding box that checks for items near the frame and converts them
        DroppedFrameLocation droppedFrameLocation = new DroppedFrameLocation(event.getEntity().getLocation());
        droppedFrames.add(droppedFrameLocation);
        droppedFrameLocation.setRemoval((new BukkitRunnable() {
            @Override
            public void run() {
                droppedFrames.remove(droppedFrameLocation);
            }
        }).runTaskLater(this, 20L));
    }

    @EventHandler
    private void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        if (item.getItemStack().getType() != Material.ITEM_FRAME && (glowFrame == null || item.getItemStack().getType() != glowFrame)) {
            return;
        }

        Iterator<DroppedFrameLocation> iter = droppedFrames.iterator();
        while (iter.hasNext()) {
            DroppedFrameLocation droppedFrameLocation = iter.next();
            if (droppedFrameLocation.isFrame(item)) {
                ItemStack frame = generateInvisibleItemFrame();
                if (glowFrame != null && item.getItemStack().getType() == glowFrame) {
                    ItemMeta meta = frame.getItemMeta();
                    meta.setDisplayName(ChatColor.WHITE + "Glow Invisible Item Frame");
                    frame.setItemMeta(meta);
                    frame.setType(glowFrame);
                }
                event.getEntity().setItemStack(frame);

                droppedFrameLocation.getRemoval().cancel();
                iter.remove();
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!framesGlow) {
            return;
        }

        if (isFrameEntity(event.getRightClicked()) &&
                event.getRightClicked().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE)) {
            ItemFrame frame = (ItemFrame) event.getRightClicked();
            Bukkit.getScheduler().runTaskLater(this, () ->
            {
                if (frame.getItem().getType() != Material.AIR) {
                    frame.setGlowing(false);
                    frame.setVisible(false);
                }
            }, 1L);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!framesGlow) {
            return;
        }

        if (isFrameEntity(event.getEntity()) &&
                event.getEntity().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE)) {
            ItemFrame frame = (ItemFrame) event.getEntity();
            Bukkit.getScheduler().runTaskLater(this, () ->
            {
                if (frame.getItem().getType() == Material.AIR) {
                    if (framesGlow) {
                        frame.setGlowing(true);
                        frame.setVisible(true);
                    }
                }
            }, 1L);
        }
    }
    public static void startSaveAndRegisterPlayer(){
        YamlConfiguration statusData = Main.getInstance().getFileManager().getStatusData();
        for (Player all : Bukkit.getOnlinePlayers()){
            all.setScoreboard(PrefixManager.getScoreboard());
        }
        for (Player all : Bukkit.getOnlinePlayers()){
            if (statusData.getString(all.getUniqueId().toString()) == null) {
                FileManager.savePlayerInStatus(all, "Default", "§f");
                PrefixManager.getScoreboard().getTeam(PrefixManager.getTeam()).addEntry(all.getDisplayName());
            }
            if (statusData.getString(all.getUniqueId() + ".status").equals("Default")){
                PrefixManager.getScoreboard().getTeam(PrefixManager.getTeam()).addEntry(all.getDisplayName());
            }else {
                PrefixManager.updatePrefix(all);
            }
        }
    }
}



