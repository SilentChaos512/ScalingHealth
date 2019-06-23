package net.silentchaos512.scalinghealth.client.particles;

import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.utils.Color;

import java.util.Locale;

public enum ModParticles {
    HEART_CRYSTAL("heart_crystal", Color.FIREBRICK, 0.08, 0.05),
    POWER_CRYSTAL("power_crystal", Color.ROYALBLUE, 0.08, 0.05),
    CURSED_HEART("cursed_heart", Color.REBECCAPURPLE, 0.08, 0.05),
    ENCHANTED_HEART("enchanted_heart", Color.ALICEBLUE, 0.08, 0.05);

    private final BasicParticleType particleType;
    private final Color color;
    private final double motionScaleX;
    private final double motionScaleY;
    private final double motionScaleZ;

    ModParticles(String name, Color color, double motionScaleXZ, double motionScaleY) {
        this.particleType = new BasicParticleType(true);
        this.color = color;
        this.motionScaleX = motionScaleXZ;
        this.motionScaleZ = motionScaleXZ;
        this.motionScaleY = motionScaleY;
    }

    public void spawn(int count, LivingEntity entity) {
        spawn(count, entity.world, entity.posX, entity.posY + 0.65 * entity.getHeight(), entity.posZ);
    }

    public void spawn(int count, IWorld world, double posX, double posY, double posZ) {
        for (int i = 0; i < count; ++i) {
            double motionX = motionScaleX * ScalingHealth.random.nextGaussian();
            double motionY = motionScaleY * ScalingHealth.random.nextGaussian();
            double motionZ = motionScaleZ * ScalingHealth.random.nextGaussian();
            world.addParticle(particleType, posX, posY, posZ, motionX, motionY, motionZ);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerAll(RegistryEvent.Register<ParticleType<?>> event) {
        // FIXME: This does not work AT ALL. As far as I can tell, we need to register an
        //  IParticleFactory, but all the methods are private.
        for (ModParticles particle : values()) {
            ResourceLocation id = ScalingHealth.getId(particle.name().toLowerCase(Locale.ROOT));
            particle.particleType.setRegistryName(id);
            ForgeRegistries.PARTICLE_TYPES.register(particle.particleType);
        }
    }
}
