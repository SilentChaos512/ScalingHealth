package net.silentchaos512.scalinghealth.utils.config;

import net.minecraft.entity.MobEntity;
import net.silentchaos512.scalinghealth.resources.mechanics.SHMechanicListener;
import net.silentchaos512.scalinghealth.resources.tags.EntityTags;
import net.silentchaos512.scalinghealth.utils.mode.MobHealthMode;
import net.silentchaos512.scalinghealth.utils.serialization.DifficultyMobEffect;

import java.util.List;

public final class SHMobs {
    private SHMobs() { throw new IllegalAccessError("Utility class"); }

    public static boolean allowsDifficultyChanges(MobEntity entity) {
        return !EntityTags.DIFFICULTY_EXEMPT.contains(entity.getType());
    }

    public static double blightChance() {
        return SHMechanicListener.getMobMechanics().blight.blightChance;
    }

    public static boolean canBecomeBlight(MobEntity entity) {
        return EnabledFeatures.blightsEnabled() && !EntityTags.BLIGHT_EXEMPT.contains(entity.getType());
    }

    public static boolean isBlight(MobEntity entity) {
        return SHDifficulty.affected(entity).isBlight();
    }

    public static double getBlightDifficultyMultiplier() {
        return SHMechanicListener.getMobMechanics().blight.blightDifficultyModifier;
    }

    public static boolean notifyBlightDeath(){
        return SHMechanicListener.getMobMechanics().blight.notifyBlightDeath;
    }

    public static double healthPassiveMultiplier(){
        return SHMechanicListener.getMobMechanics().generic.passiveMultiplier;
    }

    public static double healthHostileMultiplier(){
        return SHMechanicListener.getMobMechanics().generic.hostileMultiplier;
    }

    public static List<DifficultyMobEffect> getMobEffects(){
        return SHMechanicListener.getMobMechanics().mobEffects;
    }

    public static double passivePotionChance(){
        return SHMechanicListener.getMobMechanics().generic.peacefulPotionChance;
    }

    public static double hostilePotionChance(){
        return SHMechanicListener.getMobMechanics().generic.hostilePotionChance;
    }

    public static MobHealthMode getHealthMode() {
        return SHMechanicListener.getMobMechanics().mode;
    }

    public static double spawnerModifier(){
        return SHMechanicListener.getMobMechanics().generic.spawnerModifier;
    }

    public static double xpBoost(){
        return SHMechanicListener.getMobMechanics().generic.xpBoost;
    }

    public static double xpBlightBoost(){
        return SHMechanicListener.getMobMechanics().blight.blightXpBoost;
    }

    public static double damageBoostScale() {
        return SHMechanicListener.getMobMechanics().generic.damageBoostScale;
    }

    public static double maxDamageBoost() {
        return SHMechanicListener.getMobMechanics().generic.maxDamageBoost;
    }
}
