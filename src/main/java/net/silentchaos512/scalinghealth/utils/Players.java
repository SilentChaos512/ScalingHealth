package net.silentchaos512.scalinghealth.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.utils.MathUtils;

/**
 * Utility class for player-related settings. Same as with {@link Difficulty}, this should put a
 * stop to the tangled mess of config references.
 */
public final class Players {
    private Players() {throw new IllegalAccessError("Utility class");}

    public static int startingHealth(EntityPlayer player) {
        return Config.get(player).player.startingHealth.get();
    }

    public static int minHealth(EntityPlayer player) {
        return Config.get(player).player.minHealth.get();
    }

    public static int maxHealth(EntityPlayer player) {
        int value = Config.get(player).player.maxHealth.get();
        return value <= 0 ? Integer.MAX_VALUE : value;
    }

    public static int maxHeartContainers(EntityPlayer player) {
        return (maxHealth(player) - startingHealth(player)) / 2;
    }

    public static int clampExtraHearts(EntityPlayer player, int value) {
        return MathUtils.clamp(value,
                (minHealth(player) - startingHealth(player)) / 2,
                (maxHealth(player) - startingHealth(player)) / 2
        );
    }

    public static boolean heartContainersIncreaseHealth(EntityPlayer player) {
        return Config.get(player).item.heartCrystalIncreaseHealth.get();
    }

    public static int levelCostToUseHeartContainer(EntityPlayer player) {
        if (player.abilities.isCreativeMode) return 0;
        return Config.get(player).item.heartCrystalLevelCost.get();
    }

    public static float heartContainerHealthRestored(EntityPlayer player) {
        return Config.get(player).item.heartCrystalHealthRestored.get().floatValue();
    }

    public static float cursedHeartAffectAmount(World world) {
        return Config.get(world).item.cursedHeartAffect.get().floatValue();
    }

    public static float enchantedHeartAffectAmount(World world) {
        return Config.get(world).item.enchantedHeartAffect.get().floatValue();
    }
}
