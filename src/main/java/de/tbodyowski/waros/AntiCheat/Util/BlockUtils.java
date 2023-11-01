package de.tbodyowski.waros.AntiCheat.Util;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockUtils {

    public static boolean isLiquid(Block block) {
        Material type = block.getType();

        return type.equals(Material.WATER) || type.equals(Material.LEGACY_STATIONARY_LAVA)
                || type.equals(Material.LAVA) || type.equals(Material.LEGACY_STATIONARY_LAVA);
    }

    public static boolean isClimbableBlock(Block block) {
        return block.getType().equals(Material.LADDER)
                || block.getType().equals(Material.VINE);
    }

    public static boolean isSlab(Block block) {
        return block.getType().getId() == 44 || block.getType().getId() == 126 || block.getType().getId() == 205 || block.getType().getId() == 182;
    }

    public static boolean isStair(Block block) {
        return block.getType().equals(Material.ACACIA_STAIRS) || block.getType().equals(Material.BIRCH_STAIRS) || block.getType().equals(Material.BRICK_STAIRS) || block.getType().equals(Material.COBBLESTONE_STAIRS) || block.getType().equals(Material.DARK_OAK_STAIRS) || block.getType().equals(Material.NETHER_BRICK_STAIRS) || block.getType().equals(Material.JUNGLE_STAIRS) || block.getType().equals(Material.QUARTZ_STAIRS) || block.getType().equals(Material.SMOOTH_QUARTZ_STAIRS) || block.getType().equals(Material.OAK_STAIRS) || block.getType().equals(Material.SANDSTONE_STAIRS) || block.getType().equals(Material.SPRUCE_STAIRS) || block.getType().getId() == 203 || block.getType().getId() == 180;
    }
}
