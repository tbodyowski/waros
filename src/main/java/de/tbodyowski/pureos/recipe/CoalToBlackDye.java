package de.tbodyowski.pureos.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class CoalToBlackDye implements CustomRecipe {

    private final JavaPlugin plugin;

    public CoalToBlackDye(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register() {
        ItemStack result = new ItemStack(Material.BLACK_DYE, 1);

        NamespacedKey key = new NamespacedKey(plugin, "CoalToBlackDye");

        ShapelessRecipe recipe = new ShapelessRecipe(key, result);

        recipe.addIngredient(Material.COAL);

        Bukkit.addRecipe(recipe);
    }
}
