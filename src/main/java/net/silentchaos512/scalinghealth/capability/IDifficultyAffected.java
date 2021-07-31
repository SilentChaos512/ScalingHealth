package net.silentchaos512.scalinghealth.capability;

import net.minecraft.world.entity.Mob;
import net.silentchaos512.scalinghealth.utils.config.SHMobs;

public interface IDifficultyAffected {
    float getDifficulty();

    void setDifficulty(Mob mob);

    void forceDifficulty(float diff);

    boolean isBlight();

    void setIsBlight(boolean value);

    void setProcessed(boolean value);

    void tick(Mob entity);

    default float affectiveDifficulty() {
        return isBlight() ? (float) SHMobs.getBlightDifficultyMultiplier() * getDifficulty() : getDifficulty();
    }
}
