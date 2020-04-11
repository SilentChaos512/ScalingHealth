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

        stack.push();
        stack.translate(fire.getPosX(), fire.getPosY() - parent.getHeight() + 0.5, fire.getPosZ());
        float f = parent.getWidth() * FIRE_SCALE;
        stack.scale(f, f, f);

        float f1 = 0.5F;
        float f2 = parent.getHeight() / f;
        float f3 = (float) (parent.getPosY() - parent.getBoundingBox().minY);
        float f4 = 0.0F;

        Quaternion rotation = new Quaternion(-this.renderManager.info.getYaw(), 0.0F, 1.0F, 0.0F);
        stack.rotate(rotation);
        stack.translate(0, 0, f2 * 0.02f);
        int i = 0;

        this.renderManager.textureManager.bindTexture(getEntityTexture(fire));
        IVertexBuilder vertexBuilder = bufferIn.getBuffer(RenderType.getEntityGlint());

        while (f2 > 0.0F) {
            boolean flag = i % 2 == 0;
            int frame = ClientTicks.ticksInGame() % 32;
            float minU = flag ? 0.5f : 0.0f;
            float minV = frame / 32f;
            float maxU = flag ? 1.0f : 0.5f;
            float maxV = (frame + 1) / 32f;

            if (flag) {
                float swap = maxU;
                maxU = minU;
                minU = swap;
            }

            Matrix4f matrix4f = stack.getLast().getMatrix();
            vertexBuilder.pos(matrix4f, f1,0.0F - f3, f4).tex(maxU, maxV).endVertex();
            vertexBuilder.pos(matrix4f,-f1,0.0F - f3, f4).tex(minU, maxV).endVertex();
            vertexBuilder.pos(matrix4f,-f1,1.4F - f3, f4).tex(maxU, minV).endVertex();
            vertexBuilder.pos(matrix4f, f1,1.4F - f3, f4).tex(minU, minV).endVertex();
            f2 -= 0.45F;
            f3 -= 0.45F;
            f1 *= 0.9F;
            f4 += 0.03F;
            ++i;
        }
        stack.pop();
        super.render(fire, entityYaw, partialTicks, stack, bufferIn, light);*/
    }

    public static class Factory implements IRenderFactory<BlightFireEntity> {
        @Override
        public EntityRenderer<? super BlightFireEntity> createRenderFor(EntityRendererManager manager) {
            return new BlightFireRenderer(manager);
        }
    }
}