package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.silentchaos512.scalinghealth.utils.Players;

public interface IPlayerData {
    int getExtraHearts();

    int getPowerCrystals();

    void setExtraHearts(PlayerEntity player, int amount);

    void setPowerCrystalCount(PlayerEntity player, int amount);

    void updateStats(PlayerEntity player);

    void tick(PlayerEntity player);

    default void addHeart(PlayerEntity player) {
        setExtraHearts(player, getExtraHearts() + 1);
    }

    default void addHearts(PlayerEntity player, int amount) {
        setExtraHearts(player, getExtraHearts() + amount);
    }

    default void addPowerCrystal(PlayerEntity player) {
        setPowerCrystalCount(player, getPowerCrystals() + 1);
    }

    default void addPowerCrystals(PlayerEntity player, int amount) {
        setPowerCrystalCount(player, getPowerCrystals() + amount);
    }

    default int getHealthModifier(PlayerEntity player) {
        return 2 * getExtraHearts() + Players.startingHealth(player) - 20;
    }

    default double getAttackDamageModifier(PlayerEntity player) {
        return getPowerCrystals() * Players.powerCrystalIncreaseAmount(player);
    }
}
