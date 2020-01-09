package net.silentchaos512.scalinghealth.utils;

import net.minecraft.entity.player.PlayerEntity;
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

    public static int startingHealth(PlayerEntity player) {
        return Config.get(player).player.startingHealth.get();
    }

    public static int minHealth(PlayerEntity player) {
        return Config.get(player).player.minHealth.get();
    }

    public static int maxHealth(PlayerEntity player) {
        int value = Config.get(player).player.maxHealth.get();
        return value <= 0 ? Integer.MAX_VALUE : value;
    }

    public static int maxHeartCrystals(PlayerEntity player) {
        return (maxHealth(player) - startingHealth(player)) / 2;
    }

    public static int maxAttackDamage(PlayerEntity player) {
        int value = Config.get(player).player.maxAttackDamage.get();
        return value <= 0 ? Integer.MAX_VALUE : value;
    }

    public static int maxPowerCrystals(PlayerEntity player) {
        return (int) ((maxAttackDamage(player) - 1) / powerCrystalIncreaseAmount(player));
    }

    public static int clampExtraHearts(PlayerEntity player, int value) {
        return MathUtils.clamp(value,
                (minHealth(player) - startingHealth(player)) / 2,
                (maxHealth(player) - startingHealth(player)) / 2
        );
    }

    public static int clampPowerCrystals(PlayerEntity player, int value) {
        return MathUtils.clamp(value,
                0,
                maxPowerCrystals(player)
        );
    }

    public static int getCrystalCountFromHealth(PlayerEntity player, float health) {
        return (int) ((health - startingHealth(player)) / 2);
    }

    public static float getHealthAfterDeath(PlayerEntity player, DimensionType deathDimension) {
        DimensionConfig config = Config.get(deathDimension);
        int deathHp = (int) EvalVars.apply(config, player, config.player.setHealthOnDeath.get());
        int maxHp = maxHealth(player);
        int minHp = minHealth(player);
        int hp = deathHp < minHp ? minHp : deathHp;
        hp = hp > maxHp ? maxHp : hp;
        return hp;
    }

    public static boolean heartCrystalsIncreaseHealth(PlayerEntity player) {
        return Config.get(player).item.heartCrystalIncreaseHealth.get();
    }

    public static double powerCrystalIncreaseAmount(PlayerEntity player) {
        return Config.get(player).item.powerCrystalDamageIncrease.get();
    }

    public static boolean powerCrystalsIncreaseDamage(PlayerEntity player) {
        return powerCrystalIncreaseAmount(player) > 0;
    }

    public static int levelCostToUseHeartCrystal(PlayerEntity player) {
        if (player.abilities.isCreativeMode) return 0;
        return Config.get(player).item.heartCrystalLevelCost.get();
    }

    public static int levelCostToUsePowerCrystal(PlayerEntity player) {
        if (player.abilities.isCreativeMode) return 0;
        return Config.get(player).item.powerCrystalLevelCost.get();
    }

    public static float heartCrystalHealthRestored(PlayerEntity player) {
        return Config.get(player).item.heartCrystalHealthRestored.get().floatValue();
    }

    public static int cursedHeartAffectAmount(World world) {
        return Config.get(world).item.cursedHeartAffect.get().intValue();
    }

    public static int enchantedHeartAffectAmount(World world) {
        return Config.get(world).item.enchantedHeartAffect.get().intValue();
    }
    public static int chanceHeartAffectAmount(World world){
        return Config.get(world).item.chanceHeartAffect.get().intValue();
    }
}
