package net.silentchaos512.scalinghealth.utils;

import net.minecraft.entity.MobEntity;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.MobPotionConfig;
import net.silentchaos512.scalinghealth.lib.MobHealthMode;
import net.silentchaos512.scalinghealth.tags.EntityTags;

public final class SHMobs {
    private SHMobs() { throw new IllegalAccessError("Utility class"); }

    public static boolean allowsDifficultyChanges(MobEntity entity) {
        return !EntityTags.DIFFICULTY_EXEMPT.contains(entity.getType());
    }

    public static double blightChance() {
        return Config.GENERAL.mobs.blightChance.get();
    }

    public static boolean canBecomeBlight(MobEntity entity) {
        return EnabledFeatures.blightsEnabled() && !EntityTags.BLIGHT_EXEMPT.contains(entity.getType());
    }

    public static boolean isBlight(MobEntity entity) {
        return SHDifficulty.affected(entity).isBlight();
    }

    public static double getBlightDifficultyMultiplier() {
        return Config.GENERAL.mobs.blightDiffModifier.get();
    }

    public static boolean notifyOnDeath(){
        return Config.GENERAL.mobs.notifyOnBlightDeath.get();
    }

    public static double healthPassiveMultiplier(){
        return Config.GENERAL.mobs.passiveMultiplier.get();
    }

    public static double healthHostileMultiplier(){
        return Config.GENERAL.mobs.hostileMultiplier.get();
    }

    public static MobPotionConfig getMobPotionConfig(){
        return Config.GENERAL.mobs.randomPotions;
    }

    public static double passivePotionChance(){
        return Config.GENERAL.mobs.peacefulPotionChance.get();
    }

    public static double hostilePotionChance(){
        return Config.GENERAL.mobs.hostilePotionChance.get();
    }

    public static MobHealthMode healthMode(){
        return Config.GENERAL.mobs.healthMode.get();
    }

    public static double spawnerHealth(){
        return Config.GENERAL.mobs.spawnerModifier.get();
    }

    public static double xpBoost(){
        return Config.GENERAL.mobs.xpBoost.get();
    }

    public static double xpBlightBoost(){
        return Config.GENERAL.mobs.xpBlightBoost.get();
    }

    public static double damageBoostScale() {
        return Config.GENERAL.mobs.damageBoostScale.get();
    }

    public static double maxDamageBoost() {
        return Config.GENERAL.mobs.maxDamageBoost.get();
    }
}
