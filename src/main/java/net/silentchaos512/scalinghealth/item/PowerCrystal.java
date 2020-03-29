package net.silentchaos512.scalinghealth.item;

import net.minecraft.entity.player.PlayerEntity;
import net.silentchaos512.scalinghealth.client.particles.ModParticles;
import net.silentchaos512.scalinghealth.init.ModSounds;
import net.silentchaos512.scalinghealth.utils.EnabledFeatures;
import net.silentchaos512.scalinghealth.utils.SHItems;
import net.silentchaos512.scalinghealth.utils.SHPlayers;

public class PowerCrystal extends StatBoosterItem {
    @Override
    int getLevelCost(PlayerEntity player) {
        return SHItems.levelCostToUsePowerCrystal(player);
    }

    @Override
    boolean isStatIncreaseAllowed(PlayerEntity player) {
        return EnabledFeatures.powerCrystalEnabled() &&
                SHPlayers.getPlayerData(player).getPowerCrystals() * SHItems.powerCrystalIncreaseAmount() < SHPlayers.maxAttackDamage();
    }

    @Override
    boolean shouldConsume(PlayerEntity player) {
        // No extra effect
        return false;
    }

    @Override
    void extraConsumeEffect(PlayerEntity player) {
        // Nada
    }

    @Override
    void increaseStat(PlayerEntity player) {
        SHPlayers.getPlayerData(player).addPowerCrystal(player);
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
