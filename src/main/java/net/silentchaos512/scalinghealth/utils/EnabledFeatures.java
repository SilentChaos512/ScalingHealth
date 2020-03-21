package net.silentchaos512.scalinghealth.utils;

import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.GameConfig;
import net.silentchaos512.scalinghealth.event.DamageScaling;

public class EnabledFeatures {
    private static Config.Common common = Config.COMMON;

    public static boolean healthCrystalEnabled(){
        return common.crystalsAddHealth.get() && SHItems.heartCrystalIncreaseAmount() > 0;
    }

    public static boolean healthCrystalRegenEnabled() {
        return common.crystalsRegenHealth.get() && SHItems.heartCrystalHpBonusRegen() > 0;
    }

    public static boolean healthXpEnabled(){
        return common.xpAddHealth.get();
    }

    public static boolean petBonusHpEnabled(){
        return common.crystalsAddPetHealth.get();
    }

    public static boolean powerCrystalEnabled(){
        return common.crystalsAddDamage.get() && SHItems.powerCrystalIncreaseAmount() > 0;
    }

    public static boolean hpCrystalsOreGenEnabled(){
        return common.hpCrystalsOreGen.get();
    }

    public static boolean powerCrystalsOreGenEnabled(){
        return common.powerCrystalsOreGen.get() && powerCrystalEnabled();
    }

    public static boolean mobHpIncreaseEnabled(){
        return common.mobHpIncrease.get() &&
                (SHMobs.healthHostileMultiplier() > 0 || SHMobs.healthPassiveMultiplier() > 0);
    }

    public static boolean mobDamageIncreaseEnabled(){
        return common.mobDamageIncrease.get() && SHMobs.maxDamageBoost() > 0;
    }

    public static boolean playerDamageScalingEnabled(){
        GameConfig.DamageScaling cfg = Config.GENERAL.damageScaling;
        return common.playerDamageScaling.get() &&
                (cfg.difficultyWeight.get() > 0 || cfg.mode.get() == DamageScaling.Mode.MAX_HEALTH); //If in max hp, dont care about diffWeight. If diffWeight is non zero. Dont care about mode.
    }

    public static boolean mobDamageScalingEnabled(){
        GameConfig.DamageScaling cfg = Config.GENERAL.damageScaling;
        return common.mobDamageScaling.get() &&
                (cfg.affectHostiles.get() || cfg.affectPeacefuls.get()) && //if both are false, no damage scaling occurs
                (cfg.difficultyWeight.get() > 0 || cfg.mode.get() == DamageScaling.Mode.MAX_HEALTH); //If in max hp, dont care about diffWeight. If diffWeight is non zero. Dont care about mode.
    }

    public static boolean difficultyEnabled(){
        return common.enableDifficulty.get() && SHDifficulty.maxValue() > 0;
    }

    public static boolean blightsEnabled(){
        return common.enableBlights.get() && SHMobs.blightChance() > 0;
    }
}
