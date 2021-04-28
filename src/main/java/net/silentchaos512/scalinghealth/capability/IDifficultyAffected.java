package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.MobEntity;
import net.silentchaos512.scalinghealth.utils.config.SHMobs;

public interface IDifficultyAffected {
    float getDifficulty();

    void setDifficulty(MobEntity mob);

    void forceDifficulty(float diff);

    boolean isBlight();

    void setIsBlight(boolean value);

    void setProcessed(boolean value);

    void tick(MobEntity entity);

    default float affectiveDifficulty() {
        return isBlight() ? (float) SHMobs.getBlightDifficultyMultiplier() * getDifficulty() : getDifficulty();
    }
}
