package de.tbodyowski.waros.manager;

import de.tbodyowski.waros.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

import static de.tbodyowski.waros.manager.PrefixManager.*;

public class EventManager implements Listener {

    private final Boolean Join_Leave_Message_on_off;
    private final Boolean UppercaseLengthLimitToggle;
    private final String JoinMessage;
    private final String LeaveMassage;
    private final Integer Prefix_LengthLimit;

    public EventManager(Plugin plugin){
        this.Join_Leave_Message_on_off = plugin.getConfig().getBoolean("Join/Leave-Message-on/off");
        this.UppercaseLengthLimitToggle = plugin.getConfig().getBoolean("Uppercase/LengthLimit-Toggle");
        this.JoinMessage = plugin.getConfig().getString("JoinMessage");
        this.LeaveMassage = plugin.getConfig().getString("LeaveMassage");
        this.Prefix_LengthLimit = plugin.getConfig().getInt("Prefix-LengthLimit");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        YamlConfiguration statusData = Main.getInstance().getFileManager().getStatusData();
        Player p = event.getPlayer();

        if (!FileManager.playerIsRegistered(p)) {
            FileManager.savePlayerInStatus(p, "Default", "§f");
            getScoreboard().getTeam(team).addEntry(p.getDisplayName());
        }

        if (Join_Leave_Message_on_off){
            if (Objects.equals(statusData.getString(p.getUniqueId() + ".status"), "Default")) {
                event.setJoinMessage(JoinMessage + " §f[" + statusData.getString(p.getUniqueId() + ".color") + "Spieler" + "§f] "
                        + statusData.getString(p.getUniqueId() + ".player"));
            }else {
                event.setJoinMessage(JoinMessage + " §f[" + statusData.getString(p.getUniqueId() + ".color")
                        + ChatColor.translateAlternateColorCodes('&', statusData.getString(p.getUniqueId() + ".status")) + "§f] "
                        + statusData.getString(p.getUniqueId() + ".player"));
            }
        }

        statusData.set(event.getPlayer().getUniqueId()+".afk",false);
        PrefixManager.updatePrefixAllPlayers();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        YamlConfiguration statusData = Main.getInstance().getFileManager().getStatusData();

        statusData.set(e.getPlayer().getUniqueId() + ".afk",false);

        Player p = e.getPlayer();
        final String m = e.getMessage().trim();
        final String message = ChatColor.translateAlternateColorCodes('&',e.getMessage());
        float uppercaseLetter = 0;
        for (int i =0; i < m.length();i++){
            if (Character.isUpperCase(m.charAt(i)) && Character.isLetter(m.charAt(i))){
                uppercaseLetter++;
            }
        }
        if (FileManager.StringIsBlocked(e.getMessage())) {
            e.setCancelled(true);
            p.sendMessage(Main.getInstance().getStatus_Prefix()+"§7Diese Nachricht enthält §9blockierte §7Wörter!");
        }else if (UppercaseLengthLimitToggle && (uppercaseLetter / (float) m.length() > 0.3 && m.length() > Prefix_LengthLimit)){
            e.setCancelled(true);
            p.sendMessage(Main.getInstance().getStatus_Prefix()+"§9Bitte benutze nicht so viele Großbuchstaben!");
        }else {
            if (statusData.getString(p.getUniqueId() + ".status").equals("Default")) {
                e.setFormat(p.getScoreboard().getTeam(team).getPrefix() + p.getDisplayName() + "§f: §r" + message);
            } else {
                e.setFormat(p.getScoreboard().getTeam(getTeamByPlayer(p)).getPrefix() + p.getDisplayName() + "§f: §r" + message);
            }

        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        YamlConfiguration statusData = Main.getInstance().getFileManager().getStatusData();

        statusData.set(event.getPlayer().getUniqueId()+".afk",false);

        Player p = event.getPlayer();

        if (Join_Leave_Message_on_off) {
            if (statusData.getString(p.getUniqueId() + ".status").equals("Default")) {
                event.setQuitMessage(LeaveMassage + " §f[" + statusData.getString(p.getUniqueId() + ".color") + "Spieler" + "§f] " + statusData.getString(p.getUniqueId() + ".player"));
            } else {
                event.setQuitMessage(LeaveMassage + " §f[" + statusData.getString(p.getUniqueId() + ".color") + ChatColor.translateAlternateColorCodes('&', statusData.getString(p.getUniqueId() + ".status")) + "§f] " + statusData.getString(p.getUniqueId() + ".player"));
            }
        }
    }

    @EventHandler
    public void onPDearth(PlayerDeathEvent event){
        if (event.getEntity() instanceof Player){
            PrefixManager.updatePrefixAllPlayers();
        }
    }

    @EventHandler
    public void onAFK(PlayerMoveEvent event){

    }
}