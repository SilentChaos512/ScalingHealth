package net.silentchaos512.scalinghealth.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.DimensionConfig;
import net.silentchaos512.scalinghealth.config.EvalVars;
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

    public static int maxHeartCrystals(EntityPlayer player) {
        return (maxHealth(player) - startingHealth(player)) / 2;
    }

    public static int maxAttackDamage(EntityPlayer player) {
        int value = Config.get(player).player.maxAttackDamage.get();
        return value <= 0 ? Integer.MAX_VALUE : value;
    }

    public static int maxPowerCrystals(EntityPlayer player) {
        return (int) ((maxAttackDamage(player) - 1) / powerCrystalIncreaseAmount(player));
    }

    public static int clampExtraHearts(EntityPlayer player, int value) {
        return MathUtils.clamp(value,
                (minHealth(player) - startingHealth(player)) / 2,
                (maxHealth(player) - startingHealth(player)) / 2
        );
    }

    public static int clampPowerCrystals(EntityPlayer player, int value) {
        return MathUtils.clamp(value,
                0,
                maxPowerCrystals(player)
        );
    }

    public static int getCrystalCountFromHealth(EntityPlayer player, float health) {
        return (int) ((health - startingHealth(player)) / 2);
    }

    public static float getHealthAfterDeath(EntityPlayer player, DimensionType deathDimension) {
        DimensionConfig config = Config.get(deathDimension);
        return (float) EvalVars.apply(config, player.world, player.getPosition(), player, config.player.setHealthOnDeath.get());
    }

    public static boolean heartCrystalsIncreaseHealth(EntityPlayer player) {
        return Config.get(player).item.heartCrystalIncreaseHealth.get();
    }

    public static double powerCrystalIncreaseAmount(EntityPlayer player) {
        return Config.get(player).item.powerCrystalDamageIncrease.get();
    }

    public static boolean powerCrystalsIncreaseDamage(EntityPlayer player) {
        return powerCrystalIncreaseAmount(player) > 0;
    }

    public static int levelCostToUseHeartCrystal(EntityPlayer player) {
        if (player.abilities.isCreativeMode) return 0;
        return Config.get(player).item.heartCrystalLevelCost.get();
    }

    public static int levelCostToUsePowerCrystal(EntityPlayer player) {
        if (player.abilities.isCreativeMode) return 0;
        return Config.get(player).item.powerCrystalLevelCost.get();
    }

    public static float heartCrystalHealthRestored(EntityPlayer player) {
        return Config.get(player).item.heartCrystalHealthRestored.get().floatValue();
    }

    public static float cursedHeartAffectAmount(World world) {
        return Config.get(world).item.cursedHeartAffect.get().floatValue();
    }

    public static float enchantedHeartAffectAmount(World world) {
        return Config.get(world).item.enchantedHeartAffect.get().floatValue();
    }
}
