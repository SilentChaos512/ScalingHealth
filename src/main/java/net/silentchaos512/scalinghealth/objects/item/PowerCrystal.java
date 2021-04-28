package net.silentchaos512.scalinghealth.objects.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.SoundEvent;
import net.silentchaos512.scalinghealth.objects.Registration;
import net.silentchaos512.scalinghealth.utils.config.EnabledFeatures;
import net.silentchaos512.scalinghealth.utils.config.SHItems;
import net.silentchaos512.scalinghealth.utils.config.SHPlayers;

public class PowerCrystal extends StatBoosterItem {
    public PowerCrystal(Properties properties) {
        super(properties);
    }

    @Override
    protected int getLevelCost(PlayerEntity player) {
        return SHItems.levelCostToUsePowerCrystal(player);
    }

    @Override
    protected boolean isStatIncreaseAllowed(PlayerEntity player) {
        return EnabledFeatures.powerCrystalEnabled() &&
                SHPlayers.getPlayerData(player).getPowerCrystals() * SHItems.powerCrystalIncreaseAmount() < SHPlayers.maxAttackDamage();
    }

    @Override
    protected boolean shouldConsume(PlayerEntity player) {
        // No extra effect
        return false;
    }

    @Override
    protected void extraConsumeEffect(PlayerEntity player) {
        // Nada
    }

    @Override
    protected void increaseStat(PlayerEntity player) {
        SHPlayers.getPlayerData(player).addPowerCrystal(player);
    }

    @Override
    protected IParticleData getParticleType() {
        return Registration.POWER_CRYSTAL_PARTICLE.get();
    }

    @Override
    protected SoundEvent getSoundEffect() {
        return Registration.HEART_CRYSTAL_USE.get();
    }
}
