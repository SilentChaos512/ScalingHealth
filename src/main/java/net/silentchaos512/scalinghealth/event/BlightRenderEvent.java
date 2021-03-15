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
import net.silentchaos512.scalinghealth.utils.EnabledFeatures;
import net.silentchaos512.scalinghealth.utils.SHDifficulty;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ScalingHealth.MOD_ID)
public class BlightRenderEvent {
   private static final float FIRE_SCALE = 1.8F;
   private static final ResourceLocation TEXTURE = ScalingHealth.getId("textures/entity/blightfire.png");
   private static final RenderType RENDER_TYPE = RenderType.getEntityCutout(TEXTURE);

   @SubscribeEvent
   public static void renderBlight(RenderLivingEvent<MobEntity, ? extends EntityModel<? extends MobEntity>> event) {
      LivingEntity entity = event.getEntity();
      if(EnabledFeatures.shouldRenderBlights() && entity instanceof MobEntity && SHDifficulty.affected(entity).isBlight()){
         MatrixStack stack = event.getMatrixStack();
         int light = event.getLight();
         MobEntity mob = (MobEntity) entity;

         stack.push();
         float w = mob.getWidth() * FIRE_SCALE;
         stack.scale(w, w, w);

         float hwRatio = mob.getHeight() / w;
         float xOffset = 0.5F;
         float yOffset = (float) (mob.getPosY() - mob.getBoundingBox().minY);
         float zOffset = 0.0F;

         Quaternion cam = event.getRenderer().getRenderManager().getCameraOrientation();
         stack.rotate(new Quaternion(0, cam.getY(), 0, cam.getW()));

         stack.translate(0, 0, hwRatio * 0.02f);
         int i = 0;

         IVertexBuilder vertexBuilder = event.getBuffers().getBuffer(RENDER_TYPE);
         Matrix4f matrix4f = stack.getLast().getMatrix();
         Matrix3f matrix3f = stack.getLast().getNormal();

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

            vertexBuilder.pos(matrix4f, xOffset,0.0F - yOffset, zOffset).color(255,255,255,255).tex(maxU, maxV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexBuilder.pos(matrix4f,-xOffset,0.0F - yOffset, zOffset).color(255,255,255,255).tex(minU, maxV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexBuilder.pos(matrix4f,-xOffset,1.4F - yOffset, zOffset).color(255,255,255,255).tex(minU, minV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexBuilder.pos(matrix4f, xOffset,1.4F - yOffset, zOffset).color(255,255,255,255).tex(maxU, minV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            hwRatio -= 0.45F;
            yOffset -= 0.45F;
            xOffset *= 0.9F;
            zOffset += 0.03F;
            ++i;
         }

         stack.pop();
      }
   }
}
