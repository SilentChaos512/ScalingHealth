package net.silentchaos512.scalinghealth.utils;

import net.minecraft.entity.MobEntity;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.MobPotionConfig;
import net.silentchaos512.scalinghealth.lib.MobHealthMode;

public final class SHMobs {
    private SHMobs() {throw new IllegalAccessError("Utility class");}

    //TODO: implement
    public static boolean allowsDifficultyChanges(MobEntity entity) {
        return true;
    }

    public static boolean canBecomeBlight(MobEntity entity) {
        return Config.get(entity.world).mobs.isMobExempt(entity);
    }

    public static boolean isBlight(MobEntity entity) {
        return SHDifficulty.affected(entity).isBlight();
    }

    public static double getBlightDifficultyMultiplier(World world) {
        return Config.get(world).mobs.blightDiffModifier.get();
    }

    public static boolean notifyOnDeath(World world){
        return Config.get(world).mobs.notifyOnBlightDeath.get();
    }

    public static double healthPassiveMutliplier(World world){
        return Config.get(world).mobs.passiveMultiplier.get();
    }

    public static double healthHostileMultiplier(World world){
        return Config.get(world).mobs.hostileMultiplier.get();
    }

    public static MobPotionConfig getMobPotionConfig(World world){
        return Config.get(world).mobs.randomPotions;
    }

    public static double passivePotionChance(World world){
        return Config.get(world).mobs.peacefulPotionChance.get();
    }

    public static double hostilePotionChance(World world){
        return Config.get(world).mobs.hostilePotionChance.get();
    }

    public static MobHealthMode healthMode(World world){
        return Config.get(world).mobs.healthMode.get();
    }

    public static double xpBoost(World world){
        return Config.get(world).mobs.xpBoost.get();
    }

    public static double xpBlightBoost(World world){
        return Config.get(world).mobs.xpBlightBoost.get();
    }

    public static double damageBoostScale(MobEntity entity) {
        return Config.get(entity).mobs.damageBoostScale.get();
    }

    public static double maxDamageBoost(MobEntity entity) {
        return Config.get(entity).mobs.maxDamageBoost.get();
    }
}
