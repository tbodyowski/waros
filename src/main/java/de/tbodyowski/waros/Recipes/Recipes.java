package de.tbodyowski.waros.Recipes;

import de.tbodyowski.waros.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class Recipes {
    private static NamespacedKey invisibleKey;
    public void registerRecipe() {

        ShapedRecipe InvisFrameRecipe = new ShapedRecipe(generateInvisibleItemFrame());
        InvisFrameRecipe.shape("GGG", "GIG", "GGG");
        InvisFrameRecipe.setIngredient('G', Material.ITEM_FRAME);
        InvisFrameRecipe.setIngredient('I', Material.GLOWSTONE_DUST);
        Bukkit.addRecipe(InvisFrameRecipe);
    }
    public static ItemStack generateInvisibleItemFrame() {
        ItemStack item = new ItemStack(Material.ITEM_FRAME, 8);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Invisible Item Frame");
        meta.getPersistentDataContainer().set(Main.getInvisibleKey(), PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }
}
