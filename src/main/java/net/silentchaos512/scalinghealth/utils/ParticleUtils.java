package net.silentchaos512.scalinghealth.utils;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.silentchaos512.scalinghealth.ScalingHealth;

public class ParticleUtils {
    public static void spawn(ParticleOptions data, int count, LivingEntity entity) {
        spawn(data, count, entity.level, entity.getX(), entity.getY() + 0.65 * entity.getBbHeight(), entity.getZ());
    }

    public static void spawn(ParticleOptions data, int count, LevelAccessor world, double posX, double posY, double posZ) {
        for (int i = 0; i < count; ++i) {
            double motionX = 0.08 * ScalingHealth.RANDOM.nextGaussian();
            double motionY = 0.05 * ScalingHealth.RANDOM.nextGaussian();
            double motionZ = 0.08 * ScalingHealth.RANDOM.nextGaussian();
            world.addParticle(data, posX, posY, posZ, motionX, motionY, motionZ);
        }
    }
}
