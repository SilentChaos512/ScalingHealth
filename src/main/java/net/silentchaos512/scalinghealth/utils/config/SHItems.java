package net.silentchaos512.scalinghealth.utils.config;

import net.minecraft.entity.player.PlayerEntity;
import net.silentchaos512.scalinghealth.resources.mechanics.SHMechanicListener;

public final class SHItems {
    private SHItems() { throw new IllegalAccessError("Utility Class"); }

    public static int heartCrystalIncreaseAmount() {
        return SHMechanicListener.getItemMechanics().heartCrystalHealthIncrease;
    }

    public static double heartCrystalHpBonusRegen() {
        return SHMechanicListener.getItemMechanics().heartCrystalBonusRegen;
    }

    public static double powerCrystalIncreaseAmount() {
        return SHMechanicListener.getItemMechanics().powerCrystalDamageIncrease;
    }

    public static int levelCostToUseHeartCrystal(PlayerEntity player) {
        if (player.abilities.isCreativeMode) return 0;
        return SHMechanicListener.getItemMechanics().heartCrystalLevelCost;
    }

    public static int levelCostToUsePowerCrystal(PlayerEntity player) {
        if (player.abilities.isCreativeMode) return 0;
        return SHMechanicListener.getItemMechanics().powerCrystalLevelCost;
    }

    public static double cursedHeartAffectAmount() {
        return SHMechanicListener.getItemMechanics().cursedHeartChange;
    }

    public static double enchantedHeartAffectAmount() {
        return SHMechanicListener.getItemMechanics().enchantedHeartChange;
    }

    public static int chanceHeartAffectAmount(){
        return SHMechanicListener.getItemMechanics().chanceHeartChange;
    }
}
