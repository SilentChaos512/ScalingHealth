package net.silentchaos512.scalinghealth.client;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.ResourceLocation;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.utils.Color;
import net.silentchaos512.utils.MathUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ColoredParticle extends TexturedParticle {
    private static final List<ResourceLocation> TEXTURES = IntStream.range(0, 4).boxed()
            .map(k -> new ResourceLocation(ScalingHealth.MOD_ID, "textures/particle/generic" + k + ".png"))
            .collect(Collectors.toList());
    private static final int[] FRAMES = {0, 1, 2, 3, 2, 1, 0};

    public ColoredParticle(ClientWorld worldIn, Color color, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        this.maxAge = 16;
        this.canCollide = false;
        this.setColor(color.getRed(), color.getGreen(), color.getBlue());
    }

    @Override
    public void renderParticle(IVertexBuilder builder, ActiveRenderInfo info, float v) {
        int frame = FRAMES.length * this.age / this.maxAge;
        int textureIndex = FRAMES[MathUtils.clamp(frame, 0, FRAMES.length - 1)];
        ResourceLocation texture = TEXTURES.get(textureIndex);
        Minecraft.getInstance().textureManager.bindTexture(texture);
        super.renderParticle(builder, info, v);
    }

    @Override
    protected float getMinU() {
        return 0;
    }

    @Override
    protected float getMaxU() {
        return 1;
    }

    @Override
    protected float getMinV() {
        return 0;
    }

    @Override
    protected float getMaxV() {
        return 1;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final Color color;
        public Factory(Color color) {
            this.color = color;
        }

        @Nullable
        @Override
        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ColoredParticle(worldIn, this.color, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}
