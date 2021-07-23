package net.silentchaos512.scalinghealth.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silentchaos512.lib.event.ClientTicks;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.utils.config.EnabledFeatures;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ScalingHealth.MOD_ID)
public class BlightRenderEvent {
   private static final float FIRE_SCALE = 1.8F;
   private static final ResourceLocation TEXTURE = ScalingHealth.getId("textures/entity/blightfire.png");
   private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE);

   @SubscribeEvent
   public static void renderBlight(RenderLivingEvent<MobEntity, ? extends EntityModel<? extends MobEntity>> event) {
      LivingEntity entity = event.getEntity();
      if(EnabledFeatures.shouldRenderBlights() && entity instanceof MobEntity && SHDifficulty.affected(entity).isBlight()){
         MatrixStack stack = event.getMatrixStack();
         int light = event.getLight();
         MobEntity mob = (MobEntity) entity;

         stack.pushPose();
         float w = mob.getBbWidth() * FIRE_SCALE;
         stack.scale(w, w, w);

         float hwRatio = mob.getBbHeight() / w;
         float xOffset = 0.5F;
         float yOffset = (float) (mob.getY() - mob.getBoundingBox().minY);
         float zOffset = 0.0F;

         Quaternion cam = event.getRenderer().getDispatcher().cameraOrientation();
         stack.mulPose(new Quaternion(0, cam.j(), 0, cam.r()));

         stack.translate(0, 0, hwRatio * 0.02f);
         int i = 0;

         IVertexBuilder vertexBuilder = event.getBuffers().getBuffer(RENDER_TYPE);
         Matrix4f matrix4f = stack.last().pose();
         Matrix3f matrix3f = stack.last().normal();

         while (hwRatio > 0.0F) {
            boolean swapU = i % 2 == 0;
            int frame = ClientTicks.ticksInGame() % 32;
            float minU = swapU ? 0.5f : 0.0f;
            float minV = frame / 32f;
            float maxU = swapU ? 1.0f : 0.5f;
            float maxV = (frame + 1) / 32f;

            if (swapU) {
               float swap = maxU;
               maxU = minU;
               minU = swap;
            }

            vertexBuilder.vertex(matrix4f, xOffset,0.0F - yOffset, zOffset).color(255,255,255,255).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexBuilder.vertex(matrix4f,-xOffset,0.0F - yOffset, zOffset).color(255,255,255,255).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexBuilder.vertex(matrix4f,-xOffset,1.4F - yOffset, zOffset).color(255,255,255,255).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexBuilder.vertex(matrix4f, xOffset,1.4F - yOffset, zOffset).color(255,255,255,255).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            hwRatio -= 0.45F;
            yOffset -= 0.45F;
            xOffset *= 0.9F;
            zOffset += 0.03F;
            ++i;
         }

         stack.popPose();
      }
   }
}
