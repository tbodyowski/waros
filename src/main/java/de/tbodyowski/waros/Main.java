package de.tbodyowski.waros;

import de.tbodyowski.waros.Events.ChatEvent;
import de.tbodyowski.waros.Events.DeathEvent;
import de.tbodyowski.waros.Events.ElytraBoostEvent;
import de.tbodyowski.waros.Events.GUIClickEvent;
import de.tbodyowski.waros.commands.*;
import de.tbodyowski.waros.inventory.admin.GuildAdminInventory;
import de.tbodyowski.waros.manager.*;
import de.tbodyowski.waros.util.DroppedFrameLocation;
import de.tbodyowski.waros.util.Websocket;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.bukkit.*;
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

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class Main extends JavaPlugin implements Listener {

    private String Status_Prefix = "";
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
    private Socket socket;
    private Websocket websocket;
    private GuildManager guildManager;
    private ConfigVarManager configVarManager;
    private GuildAdminInventory guildAdminInventory;


    @Override
    public void onEnable() {
        saveDefaultConfig();
        initSocket();
        instance = this;
        this.prefixManager = new PrefixManager();
        this.configVarManager = new ConfigVarManager();
        this.guildAdminInventory = new GuildAdminInventory();
        this.fileManager = new FileManager();
        guildManager = new GuildManager();
        invisibleRecipe = new NamespacedKey(this, "invisible-recipe");
        invisibleKey = new NamespacedKey(this, "invisible");
        configVarManager.updateVar();

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
        getServer().getPluginManager().registerEvents(new ChatEvent(), this);
        getServer().getPluginManager().registerEvents(new GUIClickEvent(), this);

        if (this.getConfig().getBoolean("Status-Prefix-on/off")) {
            this.Status_Prefix = this.getConfig().getString("Status-Prefix");
        }
        this.DeathCounter_on_off = this.getConfig().getBoolean("DeathCounter-on/off");

        Bukkit.getPluginManager().registerEvents(new EventManager(),this);
        getCommand("admin").setExecutor(new AdminCommand());
        getCommand("status").setExecutor(new StatusCommand());
        getCommand("status").setTabCompleter(new StatusTabComplete());
        getCommand("guild").setExecutor(new GuildCommand(guildManager));
        getCommand("guild").setTabCompleter(new GuildTabComplete(guildManager));
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
        removeRecipe();
        if (this.getFileManager() != null) {
            this.getFileManager().saveStatusFile();
            this.getFileManager().saveBlockedWordsFile();
        } else {
            getLogger().warning("FileManager is null during shutdown. Skipping file save.");
        }
        saveDefaultConfig();
    }

    private void initSocket(){
        try {
            socket = IO.socket("http://localhost:3000");
            websocket = new Websocket(socket);
            websocket.connect();
        } catch (URISyntaxException e) {
            Logger.getLogger("minecraft").log(Level.SEVERE, "URISyntaxException while connecting to WebSocket", e);
            getServer().getPluginManager().disablePlugin(this);
        } catch (Exception e) {
            Logger.getLogger("minecraft").log(Level.SEVERE, "Exception while connecting to WebSocket", e);
            getServer().getPluginManager().disablePlugin(this);
        }
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

    public Boolean getDeathCounter_on_off() {
        return DeathCounter_on_off;
    }

    public static Main getInstance() {
        return instance;
    }

    public GuildAdminInventory getGuildAdminInventory() {
        return guildAdminInventory;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }

    public String getStatus_Prefix() {
        return Status_Prefix;
    }

    public Websocket getWebsocket() {
        return websocket;
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