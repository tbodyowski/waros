package de.tbodyowski.waros.commands;

import de.tbodyowski.waros.Main;
import de.tbodyowski.waros.manager.GuildManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuildCommand implements CommandExecutor {

    private final GuildManager guildManager;

    public GuildCommand(GuildManager guildManager) {
        this.guildManager = guildManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /guild <create|invite|accept|leave|list|delete> [args]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /guild create <guild name>");
                    return true;
                }
                String createGuildName = args[1];
                if (guildManager.createGuild(createGuildName, player)) {
                    player.sendMessage(ChatColor.GREEN + "Guild " + createGuildName + " created successfully.");
                } else {
                    player.sendMessage(ChatColor.RED + "A guild with that name already exists.");
                }
                break;

            case "invite":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /guild invite <player name>");
                    return true;
                }
                Player invitee = player.getServer().getPlayer(args[1]);
                if (invitee == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                if (guildManager.invitePlayer(guildManager.getGuildByPlayer(player.getUniqueId()), player, invitee)) {
                    invitee.sendMessage(ChatColor.YELLOW + "You have been invited to join the guild " + guildManager.getGuildByPlayer(player.getUniqueId()));
                    player.sendMessage(ChatColor.GREEN + "Invitation sent to " + invitee.getName());
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to send the invitation.");
                }
                break;

            case "accept":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /guild accept <guild name>");
                    return true;
                }
                String acceptGuildName = args[1];
                if (guildManager.acceptInvite(acceptGuildName, player)) {
                    player.sendMessage(ChatColor.GREEN + "You have joined the guild " + acceptGuildName);
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to accept the invitation.");
                }
                break;

            case "leave":
                String leaveGuildName = guildManager.getGuildByPlayer(player.getUniqueId());
                if (Main.getInstance().getGuildManager().getOwner(leaveGuildName) == player){
                    player.sendMessage(ChatColor.RED + "You cannot leave the guild because you are the owner");
                    return false;
                }
                if (guildManager.leaveGuild(leaveGuildName, player)) {
                    player.sendMessage(ChatColor.GREEN + "You have left the guild " + leaveGuildName);
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to leave the guild.");
                }
                break;

            case "list":
                String guildName = guildManager.getGuildByPlayer(player.getUniqueId());
                if (guildName == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a guild.");
                } else {
                    player.sendMessage(ChatColor.GOLD + "Members of guild " + guildName + ":");
                    for (Player member : guildManager.getPlayersInGuild(guildName)) {
                        player.sendMessage(ChatColor.GRAY + "- " + member.getName());
                    }
                }
                break;

            case "delete":
                String guildToDelete = guildManager.getGuildByPlayer(player.getUniqueId());
                    if (guildManager.confirmGuildDeletion(guildToDelete, player)) {
                        player.sendMessage(ChatColor.GREEN + "Guild " + guildToDelete + " has been deleted.");
                    } else {
                        player.sendMessage(ChatColor.RED + "Failed to delete the guild.");
                    }
                break;

            default:
                player.sendMessage(ChatColor.RED + "Unknown sub-command.");
                break;
        }

        return true;
    }
}