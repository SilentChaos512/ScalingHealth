/*
 * Scaling Health
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.scalinghealth.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.silentchaos512.lib.event.ClientTicks;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.entity.BlightFireEntity;

import javax.annotation.Nonnull;

public final class BlightFireRenderer extends EntityRenderer<BlightFireEntity> {
    private static final float FIRE_SCALE = 1.8F;

    private static final ResourceLocation TEXTURE = ScalingHealth.getId("textures/entity/blightfire.png");

    private BlightFireRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(BlightFireEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRender(BlightFireEntity fire, ClippingHelperImpl camera, double camX, double camY, double camZ) {
        MobEntity parent = (MobEntity) fire.getRidingEntity();
        if (parent == null)
            return false;

        AxisAlignedBB boundingBox = parent.getRenderBoundingBox().grow(0.5D);

        if (boundingBox.hasNaN() || boundingBox.getAverageEdgeLength() == 0.0D) {
            boundingBox = new AxisAlignedBB(
                    parent.getPosX() - 2.0D, parent.getPosY() - 2.0D, parent.getPosZ() - 2.0D,
                    parent.getPosX() + 2.0D, parent.getPosY() + 2.0D, parent.getPosZ() + 2.0D);
        }

        return parent.isInRangeToRender3d(camX, camY, camZ)
                && (parent.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(boundingBox));
    }

    @Override
    public void render(BlightFireEntity fire, float entityYaw, float partialTicks, MatrixStack stack, IRenderTypeBuffer bufferIn, int light) {
        /*MobEntity parent = (MobEntity) fire.getRidingEntity();
        if (parent == null) return;
        /*
        stack.push();
        stack.translate(fire.getPosX(), fire.getPosY() - parent.getHeight() + 0.5, fire.getPosZ());
        float f = parent.getWidth() * FIRE_SCALE * 100f;
        stack.scale(f, f, f);

        IVertexBuilder vertexBuilder = bufferIn.getBuffer(RenderType.getTranslucent());

        float f1 = 0.5F;
        float f2 = 0.0F;
        float f3 = parent.getHeight() / f;
        float f4 = (float) (parent.getPosY() - parent.getBoundingBox().minY);

        stack.rotate(new Quaternion((float) -this.renderManager.info.getRenderViewEntity().getPosY(), 0.0F, 1.0F, 0.0F));
        stack.translate(0, 0, f3 * 0.02f);

        float f5 = 0.0F;
        int i = 0;

        this.renderManager.textureManager.bindTexture(getEntityTexture(fire));

        while (f3 > 0.0F) {
            boolean flag = i % 2 == 0;
            int frame = ClientTicks.ticksInGame() % 32;
            float minU = flag ? 0.5f : 0.0f;
            float minV = frame / 32f;
            float maxU = flag ? 1.0f : 0.5f;
            float maxV = (frame + 1) / 32f;

            if (flag) {
                float f10 = maxU;
                maxU = minU;
                minU = f10;
            }

            Matrix4f matrix4f = stack.getLast().getMatrix();
            Matrix3f matrix3f = stack.getLast().getNormal();

            vertexBuilder.pos(matrix4f,f1 - f2, 0.0F - f4, f5).color(1,1, 1, 1).tex(maxU, maxV).lightmap(light).normal(matrix3f, 1 ,0, -1).endVertex();
            vertexBuilder.pos(matrix4f, -f1 - f2, 0.0F - f4, f5).color(1,1, 1, 1).tex(minU, maxV).lightmap(light).normal(matrix3f, 1 ,0, 1).endVertex();
            vertexBuilder.pos(matrix4f,-f1 - f2, 1.4F - f4, f5).color(1,1, 1, 1).tex(minU, minV).lightmap(light).normal(matrix3f, -1 ,0, 1).endVertex();
            vertexBuilder.pos(matrix4f, - f2, 1.4F - f4, f5).color(1,1, 1, 1).tex(maxU, minV).lightmap(light).normal(matrix3f, -1 ,0, 1).endVertex();
            f3 -= 0.45F;
            f4 -= 0.45F;
            f1 *= 0.9F;
            f5 += 0.03F;
            ++i;
        }
        stack.pop();
        */
    }

    public static class Factory implements IRenderFactory<BlightFireEntity> {
        @Override
        public EntityRenderer<? super BlightFireEntity> createRenderFor(EntityRendererManager manager) {
            return new BlightFireRenderer(manager);
        }
    }
}