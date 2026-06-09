package de.tbodyowski.pureos.manager;

import de.tbodyowski.pureos.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventorySeeManager implements Listener {

    private static final int[] INVSEE_TOP_TO_STORAGE = {
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44,
            45, 46, 47, 48, 49, 50, 51, 52, 53
    };

    private static final int SLOT_HELMET = 0;
    private static final int SLOT_CHESTPLATE = 1;
    private static final int SLOT_LEGGINGS = 2;
    private static final int SLOT_BOOTS = 3;
    private static final int SLOT_OFFHAND = 4;

    private final Main plugin;
    private final File dataFile;
    private final YamlConfiguration dataConfig;
    private final Map<UUID, Session> sessionsByViewer = new HashMap<>();

    public InventorySeeManager(Main plugin) {
        this.plugin = plugin;

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.dataFile = new File(plugin.getDataFolder(), "inventories.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Konnte inventories.yml nicht erstellen: " + e.getMessage());
            }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void openInvSee(Player viewer, OfflinePlayer target) {
        Inventory inventory = Bukkit.createInventory(viewer, 54, "Invsee: " + safeName(target));
        Session session = new Session(viewer.getUniqueId(), target.getUniqueId(), SessionType.INVSEE, inventory);
        sessionsByViewer.put(viewer.getUniqueId(), session);

        fillInvSeeFromSource(session);
        viewer.openInventory(inventory);
        startLiveUpdatesIfNeeded(session);

        if (!target.isOnline() && !hasStoredSnapshot(target.getUniqueId())) {
            viewer.sendMessage("§eKeine gespeicherten Inventardaten vorhanden. Du bearbeitest eine neue Offline-Kopie.");
        }
    }

    public void openEndSee(Player viewer, OfflinePlayer target) {
        Inventory inventory = Bukkit.createInventory(viewer, 27, "Endsee: " + safeName(target));
        Session session = new Session(viewer.getUniqueId(), target.getUniqueId(), SessionType.ENDSEE, inventory);
        sessionsByViewer.put(viewer.getUniqueId(), session);

        fillEndSeeFromSource(session);
        viewer.openInventory(inventory);
        startLiveUpdatesIfNeeded(session);

        if (!target.isOnline() && !hasStoredSnapshot(target.getUniqueId())) {
            viewer.sendMessage("§eKeine gespeicherten Enderchest-Daten vorhanden. Du bearbeitest eine neue Offline-Kopie.");
        }
    }

    public void shutdown() {
        for (Session session : sessionsByViewer.values()) {
            if (session.task != null) {
                session.task.cancel();
            }
            applyGuiToTarget(session);
        }
        sessionsByViewer.clear();

        for (Player player : Bukkit.getOnlinePlayers()) {
            saveOnlineSnapshot(player, false);
        }
        saveDataFile();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) {
            return;
        }

        Session session = sessionsByViewer.get(viewer.getUniqueId());
        if (session == null || event.getView().getTopInventory() != session.inventory) {
            return;
        }

        int rawSlot = event.getRawSlot();
        int topSize = event.getView().getTopInventory().getSize();

        if (event.isShiftClick()) {
            event.setCancelled(true);
            return;
        }

        if (rawSlot < topSize) {
            if (!isEditableTopSlot(session.type, rawSlot)) {
                event.setCancelled(true);
                return;
            }
        }

        Bukkit.getScheduler().runTask(plugin, () -> applyGuiToTarget(session));
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) {
            return;
        }

        Session session = sessionsByViewer.get(viewer.getUniqueId());
        if (session == null || event.getView().getTopInventory() != session.inventory) {
            return;
        }

        int topSize = event.getView().getTopInventory().getSize();
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= topSize) {
                continue;
            }
            if (!isEditableTopSlot(session.type, rawSlot)) {
                event.setCancelled(true);
                return;
            }
        }

        Bukkit.getScheduler().runTask(plugin, () -> applyGuiToTarget(session));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player viewer)) {
            return;
        }

        Session session = sessionsByViewer.get(viewer.getUniqueId());
        if (session == null || event.getInventory() != session.inventory) {
            return;
        }

        applyGuiToTarget(session);
        if (session.task != null) {
            session.task.cancel();
        }
        sessionsByViewer.remove(viewer.getUniqueId());
        saveDataFile();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        saveOnlineSnapshot(event.getPlayer(), false);
        saveDataFile();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (dataConfig.getBoolean(basePath(uuid) + ".dirty", false)) {
            Snapshot snapshot = loadSnapshot(uuid);
            applySnapshotToOnlinePlayer(player, snapshot);
            dataConfig.set(basePath(uuid) + ".dirty", false);
            saveOnlineSnapshot(player, false);
            saveDataFile();
            return;
        }

        saveOnlineSnapshot(player, false);
        saveDataFile();
    }

    private void startLiveUpdatesIfNeeded(Session session) {
        if (Bukkit.getPlayer(session.targetId) == null) {
            return;
        }

        session.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player viewer = Bukkit.getPlayer(session.viewerId);
            if (viewer == null) {
                endSession(session.viewerId);
                return;
            }

            Session current = sessionsByViewer.get(session.viewerId);
            if (current == null || current.inventory != viewer.getOpenInventory().getTopInventory()) {
                endSession(session.viewerId);
                return;
            }

            Player target = Bukkit.getPlayer(session.targetId);
            if (target == null) {
                session.task.cancel();
                session.task = null;
                return;
            }

            if (session.type == SessionType.INVSEE) {
                fillInvSeeFromOnline(session, target);
            } else {
                fillEndSeeFromOnline(session, target);
            }
        }, 10L, 10L);
    }

    private void endSession(UUID viewerId) {
        Session session = sessionsByViewer.remove(viewerId);
        if (session != null && session.task != null) {
            session.task.cancel();
        }
    }

    private void fillInvSeeFromSource(Session session) {
        Player onlineTarget = Bukkit.getPlayer(session.targetId);
        if (onlineTarget != null) {
            fillInvSeeFromOnline(session, onlineTarget);
            return;
        }

        fillInvSeeFromSnapshot(session, loadSnapshot(session.targetId));
    }

    private void fillEndSeeFromSource(Session session) {
        Player onlineTarget = Bukkit.getPlayer(session.targetId);
        if (onlineTarget != null) {
            fillEndSeeFromOnline(session, onlineTarget);
            return;
        }

        fillEndSeeFromSnapshot(session, loadSnapshot(session.targetId));
    }

    private void fillInvSeeFromOnline(Session session, Player target) {
        PlayerInventory playerInventory = target.getInventory();
        ItemStack[] storage = ensureSize(playerInventory.getStorageContents(), 36);
        ItemStack[] armor = ensureSize(playerInventory.getArmorContents(), 4);

        for (int i = 0; i < INVSEE_TOP_TO_STORAGE.length; i++) {
            session.inventory.setItem(INVSEE_TOP_TO_STORAGE[i], cloneOrNull(storage[i]));
        }

        session.inventory.setItem(SLOT_HELMET, cloneOrNull(armor[3]));
        session.inventory.setItem(SLOT_CHESTPLATE, cloneOrNull(armor[2]));
        session.inventory.setItem(SLOT_LEGGINGS, cloneOrNull(armor[1]));
        session.inventory.setItem(SLOT_BOOTS, cloneOrNull(armor[0]));
        session.inventory.setItem(SLOT_OFFHAND, cloneOrNull(playerInventory.getItemInOffHand()));

        for (int slot = 5; slot < 18; slot++) {
            session.inventory.setItem(slot, placeholderPane());
        }
    }

    private void fillInvSeeFromSnapshot(Session session, Snapshot snapshot) {
        ItemStack[] storage = ensureSize(snapshot.storage, 36);
        ItemStack[] armor = ensureSize(snapshot.armor, 4);

        for (int i = 0; i < INVSEE_TOP_TO_STORAGE.length; i++) {
            session.inventory.setItem(INVSEE_TOP_TO_STORAGE[i], cloneOrNull(storage[i]));
        }

        session.inventory.setItem(SLOT_HELMET, cloneOrNull(armor[3]));
        session.inventory.setItem(SLOT_CHESTPLATE, cloneOrNull(armor[2]));
        session.inventory.setItem(SLOT_LEGGINGS, cloneOrNull(armor[1]));
        session.inventory.setItem(SLOT_BOOTS, cloneOrNull(armor[0]));
        session.inventory.setItem(SLOT_OFFHAND, cloneOrNull(snapshot.offhand));

        for (int slot = 5; slot < 18; slot++) {
            session.inventory.setItem(slot, placeholderPane());
        }
    }

    private void fillEndSeeFromOnline(Session session, Player target) {
        ItemStack[] ender = ensureSize(target.getEnderChest().getContents(), 27);
        for (int slot = 0; slot < 27; slot++) {
            session.inventory.setItem(slot, cloneOrNull(ender[slot]));
        }
    }

    private void fillEndSeeFromSnapshot(Session session, Snapshot snapshot) {
        ItemStack[] ender = ensureSize(snapshot.ender, 27);
        for (int slot = 0; slot < 27; slot++) {
            session.inventory.setItem(slot, cloneOrNull(ender[slot]));
        }
    }

    private void applyGuiToTarget(Session session) {
        Player onlineTarget = Bukkit.getPlayer(session.targetId);
        if (session.type == SessionType.INVSEE) {
            applyInvSee(session, onlineTarget);
        } else {
            applyEndSee(session, onlineTarget);
        }
    }

    private void applyInvSee(Session session, Player onlineTarget) {
        ItemStack[] storage = new ItemStack[36];
        for (int i = 0; i < INVSEE_TOP_TO_STORAGE.length; i++) {
            storage[i] = cloneOrNull(session.inventory.getItem(INVSEE_TOP_TO_STORAGE[i]));
        }

        ItemStack[] armor = new ItemStack[4];
        armor[3] = cloneOrNull(session.inventory.getItem(SLOT_HELMET));
        armor[2] = cloneOrNull(session.inventory.getItem(SLOT_CHESTPLATE));
        armor[1] = cloneOrNull(session.inventory.getItem(SLOT_LEGGINGS));
        armor[0] = cloneOrNull(session.inventory.getItem(SLOT_BOOTS));
        ItemStack offhand = cloneOrNull(session.inventory.getItem(SLOT_OFFHAND));

        if (onlineTarget != null) {
            PlayerInventory inventory = onlineTarget.getInventory();
            inventory.setStorageContents(storage);
            inventory.setArmorContents(armor);
            inventory.setItemInOffHand(offhand);
            onlineTarget.updateInventory();
            saveOnlineSnapshot(onlineTarget, false);
            return;
        }

        Snapshot snapshot = loadSnapshot(session.targetId);
        snapshot.storage = storage;
        snapshot.armor = armor;
        snapshot.offhand = offhand;
        saveSnapshot(session.targetId, snapshot, true);
    }

    private void applyEndSee(Session session, Player onlineTarget) {
        ItemStack[] ender = new ItemStack[27];
        for (int slot = 0; slot < 27; slot++) {
            ender[slot] = cloneOrNull(session.inventory.getItem(slot));
        }

        if (onlineTarget != null) {
            onlineTarget.getEnderChest().setContents(ender);
            saveOnlineSnapshot(onlineTarget, false);
            return;
        }

        Snapshot snapshot = loadSnapshot(session.targetId);
        snapshot.ender = ender;
        saveSnapshot(session.targetId, snapshot, true);
    }

    private void saveOnlineSnapshot(Player player, boolean dirty) {
        Snapshot snapshot = new Snapshot();
        PlayerInventory inventory = player.getInventory();
        snapshot.storage = ensureSize(inventory.getStorageContents(), 36);
        snapshot.armor = ensureSize(inventory.getArmorContents(), 4);
        snapshot.offhand = cloneOrNull(inventory.getItemInOffHand());
        snapshot.ender = ensureSize(player.getEnderChest().getContents(), 27);
        saveSnapshot(player.getUniqueId(), snapshot, dirty);
    }

    private void applySnapshotToOnlinePlayer(Player player, Snapshot snapshot) {
        PlayerInventory inventory = player.getInventory();
        inventory.setStorageContents(ensureSize(snapshot.storage, 36));
        inventory.setArmorContents(ensureSize(snapshot.armor, 4));
        inventory.setItemInOffHand(cloneOrNull(snapshot.offhand));
        player.getEnderChest().setContents(ensureSize(snapshot.ender, 27));
        player.updateInventory();
    }

    private Snapshot loadSnapshot(UUID uuid) {
        Snapshot snapshot = new Snapshot();
        String base = basePath(uuid);

        snapshot.storage = readItemArray(base + ".storage", 36);
        snapshot.armor = readItemArray(base + ".armor", 4);
        snapshot.offhand = cloneOrNull(dataConfig.getItemStack(base + ".offhand"));
        snapshot.ender = readItemArray(base + ".ender", 27);
        return snapshot;
    }

    private void saveSnapshot(UUID uuid, Snapshot snapshot, boolean dirty) {
        String base = basePath(uuid);
        writeItemArray(base + ".storage", ensureSize(snapshot.storage, 36));
        writeItemArray(base + ".armor", ensureSize(snapshot.armor, 4));
        dataConfig.set(base + ".offhand", cloneOrNull(snapshot.offhand));
        writeItemArray(base + ".ender", ensureSize(snapshot.ender, 27));
        dataConfig.set(base + ".dirty", dirty);
    }

    private ItemStack[] readItemArray(String path, int size) {
        ItemStack[] result = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            result[i] = cloneOrNull(dataConfig.getItemStack(path + "." + i));
        }
        return result;
    }

    private void writeItemArray(String path, ItemStack[] items) {
        for (int i = 0; i < items.length; i++) {
            dataConfig.set(path + "." + i, cloneOrNull(items[i]));
        }
    }

    private boolean hasStoredSnapshot(UUID uuid) {
        return dataConfig.contains(basePath(uuid) + ".storage")
                || dataConfig.contains(basePath(uuid) + ".ender")
                || dataConfig.contains(basePath(uuid) + ".offhand");
    }

    private String basePath(UUID uuid) {
        return "players." + uuid;
    }

    private boolean isEditableTopSlot(SessionType type, int slot) {
        if (type == SessionType.ENDSEE) {
            return slot >= 0 && slot < 27;
        }

        if (slot == SLOT_HELMET || slot == SLOT_CHESTPLATE || slot == SLOT_LEGGINGS || slot == SLOT_BOOTS || slot == SLOT_OFFHAND) {
            return true;
        }

        for (int mapped : INVSEE_TOP_TO_STORAGE) {
            if (mapped == slot) {
                return true;
            }
        }
        return false;
    }

    private ItemStack[] ensureSize(ItemStack[] source, int size) {
        ItemStack[] copy = new ItemStack[size];
        if (source == null) {
            return copy;
        }

        for (int i = 0; i < size && i < source.length; i++) {
            copy[i] = cloneOrNull(source[i]);
        }
        return copy;
    }

    private ItemStack placeholderPane() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        pane.editMeta(meta -> meta.setDisplayName(" "));
        return pane;
    }

    private ItemStack cloneOrNull(ItemStack itemStack) {
        return itemStack == null ? null : itemStack.clone();
    }

    private String safeName(OfflinePlayer target) {
        return target.getName() == null ? target.getUniqueId().toString() : target.getName();
    }

    private void saveDataFile() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Konnte inventories.yml nicht speichern: " + e.getMessage());
        }
    }

    private static class Snapshot {
        private ItemStack[] storage = new ItemStack[36];
        private ItemStack[] armor = new ItemStack[4];
        private ItemStack offhand;
        private ItemStack[] ender = new ItemStack[27];
    }

    private static class Session {
        private final UUID viewerId;
        private final UUID targetId;
        private final SessionType type;
        private final Inventory inventory;
        private BukkitTask task;

        private Session(UUID viewerId, UUID targetId, SessionType type, Inventory inventory) {
            this.viewerId = viewerId;
            this.targetId = targetId;
            this.type = type;
            this.inventory = inventory;
        }
    }

    private enum SessionType {
        INVSEE,
        ENDSEE
    }
}

