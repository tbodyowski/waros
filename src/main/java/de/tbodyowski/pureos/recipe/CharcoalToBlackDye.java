package de.tbodyowski.pureos.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class CharcoalToBlackDye implements CustomRecipe {

    private final JavaPlugin plugin;

    public CharcoalToBlackDye(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register() {
        ItemStack result = new ItemStack(Material.BLACK_DYE, 1);

        NamespacedKey key = new NamespacedKey(plugin, "CharCoalToBlackDye");

        ShapelessRecipe recipe = new ShapelessRecipe(key, result);

        recipe.addIngredient(Material.CHARCOAL);

        Bukkit.addRecipe(recipe);
    }
}
