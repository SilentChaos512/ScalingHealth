package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.player.EntityPlayer;

public interface IPlayerData {
    int getExtraHearts();

    void setExtraHearts(EntityPlayer player, int value);

    void tick(EntityPlayer player);

    default void addHeart(EntityPlayer player) {
        setExtraHearts(player, getExtraHearts() + 1);
    }

    default void addHearts(EntityPlayer player, int amount) {
        setExtraHearts(player, getExtraHearts() + amount);
    }

    default int getHealthModifier() {
        return 2 * getExtraHearts();
    }
}
