package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.MobEntity;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.utils.SHMobs;

public interface IDifficultyAffected {
    float getDifficulty();

    void setDifficulty(MobEntity mob);

    void forceDifficulty(float diff);

    boolean isBlight();

    void setIsBlight(boolean value);

    void setProcessed(boolean value);

    void tick(MobEntity entity);

    default int getDisplayLevel() {
        return (int) (getDifficulty() / 3);
    }

    default float affectiveDifficulty(World world) {
        return isBlight() ? (float) SHMobs.getBlightDifficultyMultiplier(world) * getDifficulty() : getDifficulty();
    }
}
