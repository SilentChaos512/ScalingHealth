package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.passive.TameableEntity;

public interface IPetData {
    void addHealth(double hp, TameableEntity pet );

    float getBonusHealth();

    void tick(TameableEntity pet );
}