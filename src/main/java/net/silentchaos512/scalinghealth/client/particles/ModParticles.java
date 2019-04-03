package net.silentchaos512.scalinghealth.client.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.utils.Color;

public enum ModParticles {
    HEART_CRYSTAL("heart_crystal", Color.FIREBRICK, 0.08, 0.05),
    POWER_CRYSTAL("power_crystal", Color.ROYALBLUE, 0.08, 0.05),
    CURSED_HEART("cursed_heart", Color.REBECCAPURPLE, 0.08, 0.05),
    ENCHANTED_HEART("enchanted_heart", Color.ALICEBLUE, 0.08, 0.05);

    private final ModParticleType particleType;
    private final Color color;
    private final double motionScaleX;
    private final double motionScaleY;
    private final double motionScaleZ;

    ModParticles(String name, Color color, double motionScaleXZ, double motionScaleY) {
        ResourceLocation id = new ResourceLocation(ScalingHealth.MOD_ID, name);
        this.particleType = new ModParticleType(id, false);
        this.color = color;
        this.motionScaleX = motionScaleXZ;
        this.motionScaleZ = motionScaleXZ;
        this.motionScaleY = motionScaleY;
    }

    public void spawn(int count, EntityLivingBase entity) {
        spawn(count, entity.world, entity.posX, entity.posY + 0.65 * entity.height, entity.posZ);
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
    public static void registerAll() {
        for (ModParticles particle : values()) {
            ScalingHealth.LOGGER.debug("Create particle factory for {}", particle);
            IParticleFactory<ModParticleType> factory = new ModParticle.Factory(particle.color);
            Minecraft.getInstance().particles.registerFactory(particle.particleType, factory);
        }
    }
}
