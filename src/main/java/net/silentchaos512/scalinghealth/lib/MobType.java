package net.silentchaos512.scalinghealth.lib;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.utils.Difficulty;

public enum MobType {
    PEACEFUL,
    HOSTILE,
    BOSS,
    BLIGHT,
    PLAYER;

    public static MobType from(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer)
            return PLAYER;
        if (!entity.isNonBoss())
            return BOSS;
        if (Difficulty.isBlight(entity))
            return BLIGHT;
        if (entity instanceof IMob)
            return HOSTILE;
        return PEACEFUL;
    }

    public double getPotionChance(EntityLivingBase entity) {
        if (this == PEACEFUL)
            return Config.get(entity).mobs.peacefulPotionChance.get();
        return Config.get(entity).mobs.hostilePotionChance.get();
    }

    public MobHealthMode getHealthMode(EntityLivingBase entity) {
        return Config.get(entity).mobs.healthMode.get();
    }
}
