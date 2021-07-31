package net.silentchaos512.scalinghealth.capability;

import net.minecraft.world.entity.TamableAnimal;

public interface IPetData {
    void addHealth(double hp, TamableAnimal pet);

    float getBonusHealth();

    void tick(TamableAnimal pet );
}