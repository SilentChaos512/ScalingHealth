package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.EntityLivingBase;

public interface IDifficultyAffected {
    float getDifficulty();

    void setDifficulty(float value);

    boolean isBlight();

    default float affectiveDifficulty() {
        // TODO: Blight difficulty config
        return isBlight() ? 3 * getDifficulty() : getDifficulty();
    }

    default int getDisplayLevel() {
        return (int) (getDifficulty() / 3);
    }

    void tick(EntityLivingBase entity);
}
