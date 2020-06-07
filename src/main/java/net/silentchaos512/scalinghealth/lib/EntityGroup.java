package net.silentchaos512.scalinghealth.lib;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.utils.EnabledFeatures;
import net.silentchaos512.scalinghealth.utils.SHMobs;

public enum EntityGroup {
    PEACEFUL,
    HOSTILE,
    BOSS,
    BLIGHT,
    PLAYER;

    public static EntityGroup from(String name) {
        for(EntityGroup group : values()){
            if(group.name().equalsIgnoreCase(name)){
                return group;
            }
        }
        throw new RuntimeException("Could not get an entity group from name: " + name);
    }

    public static EntityGroup from(LivingEntity entity) {
        return from(entity, false);
    }

    public static EntityGroup from(LivingEntity entity, boolean ignoreBlightStatus) {
        if (entity instanceof PlayerEntity)
            return PLAYER;
        if (!entity.isNonBoss())
            return BOSS;
        if (entity instanceof IMob) {
        if (!ignoreBlightStatus && SHMobs.isBlight((MobEntity) entity))
            return BLIGHT;
        return HOSTILE;
    }
        return PEACEFUL;
}

    public double getPotionChance() {
        if (this == PEACEFUL)
            return SHMobs.passivePotionChance();
        return SHMobs.hostilePotionChance();
    }

    public MobHealthMode getHealthMode() {
        return SHMobs.healthMode();
    }

    public boolean isAffectedByDamageScaling() {
        switch (this) {
            case PLAYER:
                return EnabledFeatures.playerDamageScalingEnabled();
            case PEACEFUL:
                return Config.GENERAL.damageScaling.affectPeacefuls.get();
            default:
                return Config.GENERAL.damageScaling.affectHostiles.get();
        }
    }
}
