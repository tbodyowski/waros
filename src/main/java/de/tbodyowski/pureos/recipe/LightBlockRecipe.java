package de.tbodyowski.pureos.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class LightBlockRecipe implements CustomRecipe {

    private final JavaPlugin plugin;

    public LightBlockRecipe(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register() {
        ItemStack result = new ItemStack(Material.LIGHT, 8);
        result.setDurability((short) 15);

        NamespacedKey key = new NamespacedKey(plugin, "lightBlockRecipe");
        ShapedRecipe recipe = new ShapedRecipe(key, result);

        recipe.shape(
                "GGG",
                "GDG",
                "GGG"
        );
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('D', Material.GLOWSTONE_DUST);

        Bukkit.addRecipe(recipe);
    }
}
