package net.silentchaos512.scalinghealth.utils.config;

import net.silentchaos512.scalinghealth.config.SHConfig;

public class EnabledFeatures {
    private static final SHConfig.Server SERVER = SHConfig.SERVER;

    public static boolean healthCrystalEnabled() {
        return SERVER.crystalsAddHealth.get();
    }

    public static boolean healthCrystalRegenEnabled() {
        return SERVER.crystalsRegenHealth.get();
    }

    public static boolean healthXpEnabled() {
        return SERVER.xpAddHealth.get();
    }

    public static boolean petBonusHpEnabled() {
        return SERVER.crystalsAddPetHealth.get();
    }

    public static boolean powerCrystalEnabled() {
        return SERVER.crystalsAddDamage.get();
    }

    public static boolean hpCrystalsOreGenEnabled() {
        return SERVER.hpCrystalsOreGen.get() && healthCrystalEnabled();
    }

    public static boolean powerCrystalsOreGenEnabled() {
        return SERVER.powerCrystalsOreGen.get() && powerCrystalEnabled();
    }

    public static boolean mobHpIncreaseEnabled() {
        return SERVER.mobHpIncrease.get();
    }

    public static boolean mobDamageIncreaseEnabled() {
        return SERVER.mobDamageIncrease.get();
    }

    public static boolean playerDamageScalingEnabled() {
        return SERVER.playerDamageScaling.get();
    }

    public static boolean mobDamageScalingEnabled() {
        return SERVER.mobDamageScaling.get();
    }

    public static boolean difficultyEnabled() {
        return SERVER.enableDifficulty.get();
    }

    public static boolean blightsEnabled() {
        return SERVER.enableBlights.get();
    }

    public static boolean shouldRenderBlights() {
        return SHConfig.CLIENT.displayBlightEffect.get();
    }
}
