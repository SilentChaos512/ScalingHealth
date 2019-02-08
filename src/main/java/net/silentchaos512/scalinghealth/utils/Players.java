package net.silentchaos512.scalinghealth.utils;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Utility class for player-related settings. Same as with {@link net.silentchaos512.scalinghealth.difficulty.Difficulty},
 * this should put a stop to the tangled mess of config references.
 */
public final class Players {
    private Players() {throw new IllegalAccessError("Utility class");}

    public static int startingHealth(EntityPlayer player) {
        return 20;
    }

    public static int minHealth(EntityPlayer player) {
        return 2;
    }

    public static int maxHealth(EntityPlayer player) {
        return Integer.MAX_VALUE;
    }

    public static int maxHeartContainers(EntityPlayer player) {
        return (maxHealth(player) - startingHealth(player)) / 2;
    }
}
