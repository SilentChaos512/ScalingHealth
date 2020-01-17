package net.silentchaos512.scalinghealth.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.config.Config;

public final class SHItems {
    private SHItems() {throw new IllegalAccessError("Utility Class");}

    public static int heartCrystalIncreaseAmount(PlayerEntity player) {
        return Config.get(player).item.heartCrystalHealthIncrease.get();
    }

    public static float heartCrystalHealthRestored(PlayerEntity player) {
        return Config.get(player).item.heartCrystalHealthRestored.get().floatValue();
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
