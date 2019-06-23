package net.silentchaos512.scalinghealth.client.particles;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.utils.Color;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@OnlyIn(Dist.CLIENT)
public class ModParticle extends TexturedParticle {
    private static final List<ResourceLocation> TEXTURES = IntStream.range(0, 4).boxed()
            .map(k -> new ResourceLocation(ScalingHealth.MOD_ID, "textures/particle/generic" + k + ".png"))
            .collect(Collectors.toList());
    private static final int[] FRAMES = {0, 1, 2, 3, 2, 1, 0};

    protected ModParticle(World worldIn, Color color, double posXIn, double posYIn, double posZIn) {
        this(worldIn, color, posXIn, posYIn, posZIn, 0, 0, 0);
    }

    public ModParticle(World worldIn, Color color, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        this.maxAge = 16;
        this.canCollide = false;
        this.setColor(color.getRed(), color.getGreen(), color.getBlue());
    }

    @Override
    public void renderParticle(BufferBuilder buffer, ActiveRenderInfo entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        /*int frame = FRAMES.length * this.age / this.maxAge;
        int textureIndex = FRAMES[MathUtils.clamp(frame, 0, FRAMES.length - 1)];
        ResourceLocation texture = TEXTURES.get(textureIndex);
        Minecraft.getInstance().textureManager.bindTexture(texture);

        // Mostly just copied from vanilla and cleaned up a bit
        float f4 = 0.1F * this.particleScale;

        float f5 = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
        float f6 = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
        float f7 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);
        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & '\uffff';
        int k = i & '\uffff';
        Vec3d[] avec3d = new Vec3d[]{
                new Vec3d(-rotationX * f4 - rotationXY * f4, -rotationZ * f4, -rotationYZ * f4 - rotationXZ * f4),
                new Vec3d(-rotationX * f4 + rotationXY * f4, rotationZ * f4, -rotationYZ * f4 + rotationXZ * f4),
                new Vec3d(rotationX * f4 + rotationXY * f4, rotationZ * f4, rotationYZ * f4 + rotationXZ * f4),
                new Vec3d(rotationX * f4 - rotationXY * f4, -rotationZ * f4, rotationYZ * f4 - rotationXZ * f4)
        };
        if (this.particleAngle != 0.0F) {
            float f8 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
            float f9 = MathHelper.cos(f8 * 0.5F);
            float f10 = MathHelper.sin(f8 * 0.5F) * (float) cameraViewDir.x;
            float f11 = MathHelper.sin(f8 * 0.5F) * (float) cameraViewDir.y;
            float f12 = MathHelper.sin(f8 * 0.5F) * (float) cameraViewDir.z;
            Vec3d vec3d = new Vec3d((double) f10, (double) f11, (double) f12);

            for (int l = 0; l < 4; ++l) {
                avec3d[l] = vec3d.scale(2.0D * avec3d[l]
                        .dotProduct(vec3d))
                        .add(avec3d[l].scale(f9 * f9 - vec3d.dotProduct(vec3d)))
                        .add(vec3d.crossProduct(avec3d[l]).scale(2.0F * f9));
            }
        }

        buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        buffer.pos(f5 + avec3d[0].x, f6 + avec3d[0].y, f7 + avec3d[0].z)
                .tex(1, 1)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
                .lightmap(j, k).endVertex();
        buffer.pos(f5 + avec3d[1].x, f6 + avec3d[1].y, f7 + avec3d[1].z)
                .tex(1, 0)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
                .lightmap(j, k).endVertex();
        buffer.pos(f5 + avec3d[2].x, f6 + avec3d[2].y, f7 + avec3d[2].z)
                .tex(0, 0)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
                .lightmap(j, k).endVertex();
        buffer.pos(f5 + avec3d[3].x, f6 + avec3d[3].y, f7 + avec3d[3].z)
                .tex(0, 1)
                .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
                .lightmap(j, k).endVertex();
        buffer.finishDrawing();*/
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.CUSTOM;
    }

    @Override
    protected float func_217563_c() {
        return 0;
    }

    @Override
    protected float func_217564_d() {
        return 0;
    }

    @Override
    protected float func_217562_e() {
        return 0;
    }

    @Override
    protected float func_217560_f() {
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<ModParticleType> {
        private final Color color;

        public Factory(Color color) {
            this.color = color;
        }

        @Nullable
        @Override
        public Particle makeParticle(ModParticleType typeIn, World worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ModParticle(worldIn, this.color, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}
