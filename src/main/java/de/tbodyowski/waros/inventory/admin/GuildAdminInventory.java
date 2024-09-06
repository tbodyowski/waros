package de.tbodyowski.waros.inventory.admin;

import de.tbodyowski.waros.Main;
import de.tbodyowski.waros.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class GuildAdminInventory {

    private final Inventory GAdminInv = Bukkit.createInventory(null, 3 * 9, "Admin Panel - Guilds");
    public final Inventory guildView = Bukkit.createInventory(null, 27, "Guild Settings");
    private final Inventory guildPlayersView = Bukkit.createInventory(null, 54, "Guild Players");

    public Inventory getGAdminInv() {
        // Clear inventory before adding items
        GAdminInv.clear();

        for (String name : Main.getInstance().getGuildManager().getAllGuildNames()) {
            Player owner = Main.getInstance().getGuildManager().getOwner(name);
            if (owner != null) {
                GAdminInv.addItem(createOwningPlayerSkull(owner, name));
            }
        }
        return GAdminInv;
    }

    public Inventory getGuildView(String guildName) {
        guildView.clear();

        // Display the guild name as the first item
        guildView.setItem(4, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(guildName).build());

        // Display members and other guild info
        guildView.setItem(11, new ItemBuilder(Material.PLAYER_HEAD).setDisplayName(guildName)
                .setLore("Members")
                .build());

        // Get the guild owner and check for null
        Player owner = Main.getInstance().getGuildManager().getOwner(guildName);
        String ownerName = (owner != null) ? owner.getName() : "Unknown";

        guildView.setItem(13, new ItemBuilder(Material.PAPER).setDisplayName("Guild Info")
                .setLore("Owner: " + ownerName)
                .build());

        guildView.setItem(15, new ItemBuilder(Material.BARRIER).setDisplayName("Delete Guild")
                .setLore("Double click to delete this guild")
                .setLore("THIS ACTION CANNOT BE UNDONE!")
                .build());

        return guildView;
    }
    public Inventory getGuildPlayersView(String guildName) {
        guildPlayersView.clear();

        // Add players to the inventory
        for (Player player : Main.getInstance().getGuildManager().getPlayersInGuild(guildName)) {
            guildPlayersView.addItem(createPlayerSkull(player));
        }

        return guildPlayersView;
    }

    private ItemStack createOwningPlayerSkull(Player player, String guildName) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            skullMeta.setDisplayName(guildName);
            skull.setItemMeta(skullMeta);
        }

        return skull;
    }
    private ItemStack createPlayerSkull(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            skullMeta.setDisplayName(player.getName());
            skull.setItemMeta(skullMeta);
        }

        return skull;
    }
}