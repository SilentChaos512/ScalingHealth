package net.silentchaos512.scalinghealth.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.utils.Color;
import net.silentchaos512.utils.MathUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ColoredParticle extends SingleQuadParticle {
    private static final List<ResourceLocation> TEXTURES = IntStream.range(0, 4).boxed()
            .map(k -> new ResourceLocation(ScalingHealth.MOD_ID, "textures/particle/generic" + k + ".png"))
            .collect(Collectors.toList());
    private static final int[] FRAMES = {0, 1, 2, 3, 2, 1, 0};

    public ColoredParticle(ClientLevel worldIn, Color color, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        this.lifetime = 16;
        this.hasPhysics = false;
        this.setColor(color.getRed(), color.getGreen(), color.getBlue());
    }

    @Override
    public void render(VertexConsumer builder, Camera info, float v) {
        int frame = FRAMES.length * this.age / this.lifetime;
        int textureIndex = FRAMES[MathUtils.clamp(frame, 0, FRAMES.length - 1)];
        RenderSystem.setShaderTexture(0, TEXTURES.get(textureIndex));
        super.render(builder, info, v);
    }

    @Override
    protected float getU0() {
        return 0;
    }

    @Override
    protected float getU1() {
        return 1;
    }

    @Override
    protected float getV0() {
        return 0;
    }

    @Override
    protected float getV1() {
        return 1;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Factory(Color color) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ColoredParticle(worldIn, this.color, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}
