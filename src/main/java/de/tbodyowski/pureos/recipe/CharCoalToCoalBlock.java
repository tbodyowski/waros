package de.tbodyowski.pureos.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class CharCoalToCoalBlock implements CustomRecipe{

    private final JavaPlugin plugin;

    public CharCoalToCoalBlock(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public void register() {
        ItemStack result = new ItemStack(Material.COAL_BLOCK, 1);

        NamespacedKey key = new NamespacedKey(plugin, "CharCoalToCoalBlock");
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(
                "CCC",
                "CCC",
                "CCC"
        );
        recipe.setIngredient('C', Material.CHARCOAL);

        Bukkit.addRecipe(recipe);
    }
}
