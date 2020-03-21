package net.silentchaos512.scalinghealth.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.silentchaos512.scalinghealth.config.Config;

public final class SHItems {
    private SHItems() { throw new IllegalAccessError("Utility Class"); }

    public static int heartCrystalIncreaseAmount() {
        return Config.GENERAL.item.heartCrystalHealthIncrease.get();
    }

    public static float heartCrystalHpBonusRegen() {
        return Config.GENERAL.item.heartCrystalHpBonusRegen.get().floatValue();
    }

    public static double powerCrystalIncreaseAmount() {
        return Config.GENERAL.item.powerCrystalDamageIncrease.get();
    }

    public static int levelCostToUseHeartCrystal(PlayerEntity player) {
        if (player.abilities.isCreativeMode) return 0;
        return Config.GENERAL.item.heartCrystalLevelCost.get();
    }

    public static int levelCostToUsePowerCrystal(PlayerEntity player) {
        if (player.abilities.isCreativeMode) return 0;
        return Config.GENERAL.item.powerCrystalLevelCost.get();
    }

    public static int cursedHeartAffectAmount() {
        return Config.GENERAL.item.cursedHeartAffect.get().intValue();
    }

    public static int enchantedHeartAffectAmount() {
        return Config.GENERAL.item.enchantedHeartAffect.get().intValue();
    }

    public static int chanceHeartAffectAmount(){
        return Config.GENERAL.item.chanceHeartAffect.get().intValue();
    }
}
