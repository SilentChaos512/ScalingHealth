package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.player.EntityPlayer;
import net.silentchaos512.scalinghealth.utils.Players;

public interface IPlayerData {
    int getExtraHearts();

    int getPowerCrystals();

    void setExtraHearts(EntityPlayer player, int amount);

    void setPowerCrystalCount(EntityPlayer player, int amount);

    void tick(EntityPlayer player);

    default void addHeart(EntityPlayer player) {
        setExtraHearts(player, getExtraHearts() + 1);
    }

    default void addHearts(EntityPlayer player, int amount) {
        setExtraHearts(player, getExtraHearts() + amount);
    }

    default void addPowerCrystal(EntityPlayer player) {
        setPowerCrystalCount(player, getPowerCrystals() + 1);
    }

    default void addPowerCrystals(EntityPlayer player, int amount) {
        setPowerCrystalCount(player, getPowerCrystals() + amount);
    }

    default int getHealthModifier(EntityPlayer player) {
        return 2 * getExtraHearts();
    }

    default double getAttackDamageModifier(EntityPlayer player) {
        return getPowerCrystals() * Players.powerCrystalIncreaseAmount(player);
    }
}
