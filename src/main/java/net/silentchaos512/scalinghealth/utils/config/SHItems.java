package net.silentchaos512.scalinghealth.utils.config;

import net.minecraft.world.entity.player.Player;
import net.silentchaos512.scalinghealth.resources.mechanics.ItemMechanics;
import net.silentchaos512.scalinghealth.resources.mechanics.SHMechanics;

public final class SHItems {
    private SHItems() { throw new IllegalAccessError("Utility Class"); }

    private static ItemMechanics getMechanics() {
        return SHMechanics.getMechanics().itemMechanics();
    }

    public static int heartCrystalIncreaseAmount() {
        return getMechanics().heartCrystalHealthIncrease();
    }

    public static double heartCrystalHpBonusRegen() {
        return getMechanics().heartCrystalBonusRegen();
    }

    public static double powerCrystalIncreaseAmount() {
        return getMechanics().powerCrystalDamageIncrease();
    }

    public static int levelCostToUseHeartCrystal(Player player) {
        if (player.isCreative()) return 0;
        return getMechanics().heartCrystalLevelCost();
    }

    public static int levelCostToUsePowerCrystal(Player player) {
        if (player.isCreative()) return 0;
        return getMechanics().powerCrystalLevelCost();
    }

    public static double cursedHeartAffectAmount() {
        return getMechanics().cursedHeartChange();
    }

    public static double enchantedHeartAffectAmount() {
        return getMechanics().enchantedHeartChange();
    }

    public static int chanceHeartAffectAmount(){
        return getMechanics().chanceHeartChange();
    }
}
