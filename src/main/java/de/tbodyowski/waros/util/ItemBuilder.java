package de.tbodyowski.waros.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ItemBuilder {
    private ItemMeta itemMeta;
    private final ItemStack itemStack;

    public ItemBuilder(Material mat) {
        this.itemStack = new ItemStack(mat);
        this.itemMeta = this.itemStack.getItemMeta();
        // Ensure ItemMeta is initialized if it was null
        if (this.itemMeta == null) {
            this.itemMeta = this.itemStack.getItemMeta(); // This will still be null if the material does not support ItemMeta
        }
    }

    public ItemBuilder setDisplayName(String s) {
        if (this.itemMeta != null) {
            this.itemMeta.setDisplayName(s);
        }
        return this;
    }

    public ItemBuilder setLore(String... s) {
        if (this.itemMeta != null) {
            this.itemMeta.setLore(Arrays.asList(s));
        }
        return this;
    }

    public ItemBuilder setUnbreakable(boolean s) {
        if (this.itemMeta != null) {
            this.itemMeta.setUnbreakable(s);
        }
        return this;
    }

    public ItemBuilder addItemFlags(ItemFlag... s) {
        if (this.itemMeta != null) {
            this.itemMeta.addItemFlags(s);
        }
        return this;
    }

    @Override
    public String toString() {
        return "ItemBuilder{" +
                "itemMeta=" + itemMeta +
                ", itemStack=" + itemStack +
                '}';
    }

    public ItemStack build() {
        if (this.itemMeta != null) {
            this.itemStack.setItemMeta(this.itemMeta);
        }
        return this.itemStack;
    }
}
