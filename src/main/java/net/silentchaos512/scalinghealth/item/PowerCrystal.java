package net.silentchaos512.scalinghealth.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.silentchaos512.scalinghealth.capability.IPlayerData;
import net.silentchaos512.scalinghealth.client.particles.ModParticles;
import net.silentchaos512.scalinghealth.init.ModSounds;
import net.silentchaos512.scalinghealth.utils.Players;

public class PowerCrystal extends StatBoosterItem {
    @Override
    int getLevelCost(EntityPlayer player) {
        return Players.levelCostToUsePowerCrystal(player);
    }

    @Override
    boolean isStatIncreaseAllowed(EntityPlayer player, IPlayerData data) {
        return Players.powerCrystalsIncreaseDamage(player);
    }

    @Override
    boolean shouldConsume(EntityPlayer player) {
        // No extra effect
        return false;
    }

    @Override
    void extraConsumeEffect(EntityPlayer player) {
        // Nada
    }

    @Override
    void increaseStat(EntityPlayer player, ItemStack stack, IPlayerData data) {
        data.addPowerCrystal(player);
    }

    @Override
    ModParticles getParticleType() {
        return ModParticles.POWER_CRYSTAL;
    }

    @Override
    ModSounds getSoundEffect() {
        return ModSounds.HEART_CRYSTAL_USE;
    }
}
