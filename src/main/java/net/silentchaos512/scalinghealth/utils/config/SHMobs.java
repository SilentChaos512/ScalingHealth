package net.silentchaos512.scalinghealth.utils.config;

import net.minecraft.world.entity.Mob;
import net.silentchaos512.scalinghealth.resources.mechanics.MobMechanics;
import net.silentchaos512.scalinghealth.resources.mechanics.SHMechanics;
import net.silentchaos512.scalinghealth.resources.tags.EntityTags;
import net.silentchaos512.scalinghealth.utils.mode.MobHealthMode;
import net.silentchaos512.scalinghealth.utils.serialization.DifficultyMobEffect;

import java.util.List;

public final class SHMobs {
    private SHMobs() { throw new IllegalAccessError("Utility class"); }

    private static MobMechanics getMechanics() {
        return SHMechanics.getMechanics().mobMechanics();
    } 

    public static boolean allowsDifficultyChanges(Mob entity) {
        return !entity.getType().is(EntityTags.DIFFICULTY_EXEMPT);
    }

    public static double blightChance() {
        return getMechanics().blight().blightChance();
    }

    public static boolean canBecomeBlight(Mob entity) {
        return EnabledFeatures.blightsEnabled() && !entity.getType().is(EntityTags.BLIGHT_EXEMPT);
    }

    public static boolean isBlight(Mob entity) {
        return SHDifficulty.affected(entity).isBlight();
    }

    public static double getBlightDifficultyMultiplier() {
        return getMechanics().blight().blightDifficultyModifier();
    }

    public static boolean notifyBlightDeath(){
        return getMechanics().blight().notifyBlightDeath();
    }

    public static double healthPassiveMultiplier(){
        return getMechanics().generic().passiveMultiplier();
    }

    public static double healthHostileMultiplier(){
        return getMechanics().generic().hostileMultiplier();
    }

    public static List<DifficultyMobEffect> getMobEffects(){
        return getMechanics().mobEffects();
    }

    public static double passivePotionChance(){
        return getMechanics().generic().peacefulPotionChance();
    }

    public static double hostilePotionChance(){
        return getMechanics().generic().hostilePotionChance();
    }

    public static MobHealthMode getHealthMode() {
        return getMechanics().mode();
    }

    public static double spawnerModifier(){
        return getMechanics().generic().spawnerModifier();
    }

    public static double xpBoost(){
        return getMechanics().generic().xpBoost();
    }

    public static double xpBlightBoost(){
        return getMechanics().blight().blightXpBoost();
    }

    public static double damageBoostScale() {
        return getMechanics().generic().damageBoostScale();
    }

    public static double maxDamageBoost() {
        return getMechanics().generic().maxDamageBoost();
    }
}
