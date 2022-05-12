package net.silentchaos512.scalinghealth.objects.item;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.silentchaos512.scalinghealth.objects.Registration;
import net.silentchaos512.scalinghealth.utils.config.EnabledFeatures;
import net.silentchaos512.scalinghealth.utils.config.SHItems;
import net.silentchaos512.scalinghealth.utils.config.SHPlayers;

public class PowerCrystal extends StatBoosterItem {
    public PowerCrystal(Properties properties) {
        super(properties);
    }

    @Override
    protected int getLevelCost(Player player) {
        return SHItems.levelCostToUsePowerCrystal(player);
    }

    @Override
    protected boolean isStatIncreaseAllowed(Player player) {
        return EnabledFeatures.powerCrystalEnabled() &&
                SHPlayers.getPlayerData(player).getPowerCrystals() * SHItems.powerCrystalIncreaseAmount() <= SHPlayers.maxAttackDamage();
    }

    @Override
    protected boolean shouldConsume(Player player) {
        // No extra effect
        return false;
    }

    @Override
    protected void extraConsumeEffect(Player player) {
        // Nada
    }

    @Override
    protected void increaseStat(Player player) {
        SHPlayers.getPlayerData(player).addPowerCrystal(player);
    }

    @Override
    protected ParticleOptions getParticleType() {
        return Registration.POWER_CRYSTAL_PARTICLE.get();
    }

    @Override
    protected SoundEvent getSoundEffect() {
        return Registration.HEART_CRYSTAL_USE.get();
    }
}
