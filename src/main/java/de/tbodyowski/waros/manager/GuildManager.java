package de.tbodyowski.waros.manager;

import de.tbodyowski.waros.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class GuildManager {

    private final FileManager fileManager;
    private final Map<UUID, List<String>> pendingInvites;

    public GuildManager() {
        this.fileManager = Main.getInstance().getFileManager();
        this.pendingInvites = new HashMap<>();
        loadGuildData();
    }

    // Load guild data from file
    private void loadGuildData() {
        YamlConfiguration guildData = fileManager.getGuildData();
        // Load data as needed
    }

    // Save guild data to file
    private void saveGuildData() {
        fileManager.saveGuildData();
    }

    // Create a guild
    public boolean createGuild(String guildName, Player owner) {
        YamlConfiguration guildData = fileManager.getGuildData();

        if (guildData.contains(guildName)) {
            return false; // Guild already exists
        }

        // Save new guild data
        guildData.createSection(guildName);
        guildData.set(guildName + ".owner", owner.getUniqueId().toString());
        List<String> members = new ArrayList<>();
        members.add(owner.getUniqueId().toString());
        guildData.set(guildName + ".members", members);
        saveGuildData();
        return true;
    }

    // Invite a player to a guild
    public boolean invitePlayer(String guildName, Player inviter, Player invitee) {
        YamlConfiguration guildData = fileManager.getGuildData();

        if (!guildData.contains(guildName)) {
            return false; // Guild does not exist
        }

        UUID inviteeId = invitee.getUniqueId();
        List<String> invites = pendingInvites.getOrDefault(inviteeId, new ArrayList<>());

        if (invites.contains(guildName)) {
            return false; // Player already invited
        }

        invites.add(guildName);
        pendingInvites.put(inviteeId, invites);
        return true;
    }

    // Add a pending invite for a player
    public void addPendingInvite(UUID playerId, String guildName) {
        List<String> invites = pendingInvites.getOrDefault(playerId, new ArrayList<>());
        if (!invites.contains(guildName)) {
            invites.add(guildName);
            pendingInvites.put(playerId, invites);
        }
    }

    // Accept an invitation
    public boolean acceptInvite(String guildName, Player player) {
        UUID playerId = player.getUniqueId();
        List<String> invites = pendingInvites.get(playerId);

        if (invites == null || !invites.contains(guildName)) {
            return false; // No such invitation
        }

        // Remove invitation and add player to the guild
        invites.remove(guildName);
        pendingInvites.put(playerId, invites);

        // Add player to the guild
        addPlayerToGuild(guildName, player);
        saveGuildData();
        return true;
    }

    // Leave a guild
    public boolean leaveGuild(String guildName, Player player) {
        String guild = getGuildByPlayer(player.getUniqueId());

        if (guild == null || !guild.equals(guildName)) {
            return false; // Player is not in the specified guild
        }

        removePlayerFromGuild(guildName, player);
        saveGuildData();
        return true;
    }

    // Delete a guild
    public boolean deleteGuild(String guildName, Player owner) {
        YamlConfiguration guildData = fileManager.getGuildData();

        if (!guildData.contains(guildName)) {
            return false; // Guild does not exist
        }

        UUID ownerId = owner.getUniqueId();
        String guildOwnerId = guildData.getString(guildName + ".owner");

        if (!ownerId.toString().equals(guildOwnerId)) {
            return false; // Player is not the owner
        }

        guildData.set(guildName, null);
        saveGuildData();
        return true;
    }
    public void deleteGuildAdmin(String guildName) {
        YamlConfiguration guildData = fileManager.getGuildData();
        if (!guildData.contains(guildName)) {
            return; // Guild does not exist
        }
        guildData.set(guildName, null);
        saveGuildData();
    }

    // Confirm guild deletion
    public boolean confirmGuildDeletion(String guildName, Player owner) {
        // Check if the guild exists and if the player is the owner
        if (deleteGuild(guildName, owner)) {
            // Notify all members
            YamlConfiguration guildData = fileManager.getGuildData();
            List<String> memberStrings = guildData.getStringList(guildName + ".members");
            for (String memberString : memberStrings) {
                Player member = Main.getInstance().getServer().getPlayer(UUID.fromString(memberString));
                if (member != null) {
                    member.sendMessage("The guild " + guildName + " has been deleted.");
                }
            }
            return true;
        }
        return false;
    }

    // Get all guild names
    public List<String> getAllGuildNames() {
        List<String> guildNames = new ArrayList<>();
        YamlConfiguration guildData = fileManager.getGuildData();

        for (String guildName : guildData.getKeys(false)) {
            guildNames.add(guildName);
        }

        return guildNames;
    }

    // Get pending invites for a player
    public List<String> getPendingInvites(UUID playerId) {
        return pendingInvites.getOrDefault(playerId, new ArrayList<>());
    }

    // Get the guild name for a player
    public String getGuildByPlayer(UUID playerId) {
        YamlConfiguration guildData = fileManager.getGuildData();

        for (String guildName : guildData.getKeys(false)) {
            List<String> members = guildData.getStringList(guildName + ".members");
            if (members.contains(playerId.toString())) {
                return guildName;
            }
        }

        return null;
    }

    // Add a player to a guild
    private void addPlayerToGuild(String guildName, Player player) {
        YamlConfiguration guildData = fileManager.getGuildData();

        if (guildData.contains(guildName)) {
            List<String> members = guildData.getStringList(guildName + ".members");
            members.add(player.getUniqueId().toString());
            guildData.set(guildName + ".members", members);
            saveGuildData();
        }
    }

    // Remove a player from a guild
    private void removePlayerFromGuild(String guildName, Player player) {
        YamlConfiguration guildData = fileManager.getGuildData();

        if (guildData.contains(guildName)) {
            List<String> members = guildData.getStringList(guildName + ".members");
            members.remove(player.getUniqueId().toString());
            guildData.set(guildName + ".members", members);
            saveGuildData();
        }
    }
    public Player getOwner(String guildName) {
        YamlConfiguration guildData = fileManager.getGuildData();

        if (guildData.contains(guildName)) {
            String ownerString = guildData.getString(guildName + ".owner");
            if (ownerString != null) {
                try {
                    UUID ownerId = UUID.fromString(ownerString);
                    return Bukkit.getPlayer(ownerId);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public List<Player> getPlayersInGuild(String guildName) {
        List<Player> players = new ArrayList<>();
        YamlConfiguration guildData = fileManager.getGuildData();

        // Check if the guild exists
        if (guildData.contains(guildName)) {
            // Get the list of player UUIDs in the guild
            List<String> memberUUIDs = guildData.getStringList(guildName + ".members");

            // Convert UUID strings to Player objects
            for (String uuidString : memberUUIDs) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        players.add(player);
                    }
                } catch (IllegalArgumentException e) {
                    // Handle invalid UUID format if needed
                    System.out.println("Invalid UUID format: " + uuidString);
                }
            }
        } else {
            System.out.println("Guild not found: " + guildName);
        }

        return players;
    }
}

