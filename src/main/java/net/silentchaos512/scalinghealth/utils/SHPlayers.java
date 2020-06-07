package net.silentchaos512.scalinghealth.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.silentchaos512.scalinghealth.capability.IPlayerData;
import net.silentchaos512.scalinghealth.capability.PlayerDataCapability;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.EvalVars;
import net.silentchaos512.scalinghealth.config.RegenConfig;
import net.silentchaos512.utils.MathUtils;

/**
 * Utility class for player-related settings. Same as with {@link SHDifficulty}, this should put a
 * stop to the tangled mess of config references.
 */
public final class SHPlayers {
    private SHPlayers() {throw new IllegalAccessError("Utility class");}

    public static IPlayerData getPlayerData(PlayerEntity entity){
        return entity.getCapability(PlayerDataCapability.INSTANCE).orElseThrow(() -> new IllegalStateException("Could not access capability"));
    }

    public static int startingHealth() {
        return Config.GENERAL.player.startingHealth.get();
    }

    public static int minHealth() {
        return Config.GENERAL.player.minHealth.get();
    }

    public static int maxHealth() {
        int value = Config.GENERAL.player.maxHealth.get();
        return value <= 0 ? Integer.MAX_VALUE : value;
    }

    public static int maxHeartCrystals() {
        return (maxHealth() - startingHealth()) / (2 * SHItems.heartCrystalIncreaseAmount());
    }

    public static int maxAttackDamage() {
        int value = Config.GENERAL.player.maxAttackDamage.get();
        return value <= 0 ? Integer.MAX_VALUE : value;
    }

    public static int maxPowerCrystals() {
        return (int) ((maxAttackDamage() - 1) / SHItems.powerCrystalIncreaseAmount());
    }

    public static int clampExtraHearts(int value) {
        return MathUtils.clamp(value,
                (minHealth() - startingHealth()) / 2,
                (maxHealth() - startingHealth()) / 2
        );
    }

    public static int clampPowerCrystals(int value) {
        return MathUtils.clamp(value,
                0,
                maxPowerCrystals()
        );
    }

    /**
     *  Given an hp, returns how many crystals are needed to get that hp. The amount of crystals can be negative.
     *  (Started at 20, want to be at 10 -> -5 crystals are needed.
     */
    public static int getCrystalCountFromHealth(float health) {
        return (int) ((health - startingHealth()) / (2 * SHItems.heartCrystalIncreaseAmount()));
    }

    public static float getHealthAfterDeath(PlayerEntity player) {
        return MathUtils.clamp((int) EvalVars.apply(player, Config.GENERAL.player.setHealthOnDeath.get()), minHealth(), maxHealth());
    }

    public static RegenConfig getRegenConfig(){
        return Config.GENERAL.player.regen;
    }

    public static int levelsPerHp(){
        return Config.GENERAL.player.levelsPerHp.get();
    }

    public static int hpPerLevel() {
        return Config.GENERAL.player.hpPerLevel.get();
    }

    public static int hpFromCurrentXp(int levels) {
        return EnabledFeatures.healthXpEnabled() ? levels / levelsPerHp() * hpPerLevel() : 0;
    }
}
