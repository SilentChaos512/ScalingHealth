package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.EntityLivingBase;

public interface IDifficultyAffected {
    float getDifficulty();

    void setDifficulty(float value);

    boolean isBlight();

    void tick(EntityLivingBase entity);
}
