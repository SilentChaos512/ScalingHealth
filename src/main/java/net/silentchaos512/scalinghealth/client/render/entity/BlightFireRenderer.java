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

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.silentchaos512.lib.event.ClientTicks;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.entity.BlightFireEntity;
import net.silentchaos512.scalinghealth.lib.module.ModuleAprilTricks;
import net.silentchaos512.utils.Color;

import javax.annotation.Nonnull;

public final class BlightFireRenderer extends EntityRenderer<BlightFireEntity> {
    private static final float FIRE_SCALE = 1.8F;

    private static final ResourceLocation TEXTURE = ScalingHealth.getId("textures/entity/blightfire.png");
    private static final ResourceLocation TEXTURE_GRAY = ScalingHealth.getId("textures/entity/blightfire_gray.png");

    private BlightFireRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Nonnull
    @Override
    protected ResourceLocation getEntityTexture(BlightFireEntity entity) {
        return ModuleAprilTricks.instance.isRightDay() && ModuleAprilTricks.instance.isEnabled()
                ? TEXTURE_GRAY : TEXTURE;
    }

    @Override
    public boolean shouldRender(BlightFireEntity fire, ICamera camera, double camX, double camY, double camZ) {
        MobEntity parent = (MobEntity) fire.getRidingEntity();
        if (parent == null)
            return false;

        AxisAlignedBB boundingBox = parent.getRenderBoundingBox().grow(0.5D);

        if (boundingBox.hasNaN() || boundingBox.getAverageEdgeLength() == 0.0D) {
            boundingBox = new AxisAlignedBB(
                    parent.posX - 2.0D, parent.posY - 2.0D, parent.posZ - 2.0D,
                    parent.posX + 2.0D, parent.posY + 2.0D, parent.posZ + 2.0D);
        }

        return parent.isInRangeToRender3d(camX, camY, camZ)
                && (parent.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(boundingBox));
    }

    @Override
    public void doRender(BlightFireEntity fire, double x, double y, double z, float entityYaw, float partialTicks) {
        MobEntity parent = (MobEntity) fire.getRidingEntity();
        if (parent == null) return;

        GlStateManager.disableLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y - parent.getHeight() + 0.5, z);
        float f = parent.getWidth() * FIRE_SCALE;
        GlStateManager.scalef(f, f, f);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        float f1 = 0.5F;
        float f2 = 0.0F;
        float f3 = parent.getHeight() / f;
        float f4 = (float) (parent.posY - parent.getBoundingBox().minY);

        GlStateManager.rotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.translatef(0, 0, f3 * 0.02f);

        if (ModuleAprilTricks.instance.isRightDay() && ModuleAprilTricks.instance.isEnabled()) {
            float changeRate = 40f + parent.getEntityId() % 80f;
            float hue = ((ClientTicks.ticksInGame() + parent.getEntityId()) % changeRate / changeRate);
            Color color = new Color(java.awt.Color.HSBtoRGB(hue, 1, 1));
            GlStateManager.color4f(color.getRed(), color.getGreen(), color.getBlue(), 1.0F);
        } else {
            GlStateManager.color4f(1, 1, 1, 1);
        }

        float f5 = 0.0F;
        int i = 0;

        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        this.bindTexture(getEntityTexture(fire));

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

            buffer.pos(f1 - f2, 0.0F - f4, f5).tex(maxU, maxV).endVertex();
            buffer.pos(-f1 - f2, 0.0F - f4, f5).tex(minU, maxV).endVertex();
            buffer.pos(-f1 - f2, 1.4F - f4, f5).tex(minU, minV).endVertex();
            buffer.pos(f1 - f2, 1.4F - f4, f5).tex(maxU, minV).endVertex();
            f3 -= 0.45F;
            f4 -= 0.45F;
            f1 *= 0.9F;
            f5 += 0.03F;
            ++i;
        }

        tessellator.draw();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
    }

    public static class Factory implements IRenderFactory<BlightFireEntity> {
        @Override
        public EntityRenderer<? super BlightFireEntity> createRenderFor(EntityRendererManager manager) {
            return new BlightFireRenderer(manager);
        }
    }
}
