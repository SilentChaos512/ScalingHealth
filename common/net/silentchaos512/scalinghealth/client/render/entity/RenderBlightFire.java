package net.silentchaos512.scalinghealth.client.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.silentchaos512.lib.client.render.BufferBuilderSL;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.client.ClientTickHandler;
import net.silentchaos512.scalinghealth.entity.EntityBlightFire;

public class RenderBlightFire extends Render<EntityBlightFire> {

  public static final float FIRE_SCALE = 1.8F;

  protected final ResourceLocation TEXTURE = new ResourceLocation(ScalingHealth.MOD_ID_LOWER,
      "textures/entity/blightfire.png");

  public RenderBlightFire(RenderManager renderManager) {

    super(renderManager);
  }

  @Override
  protected ResourceLocation getEntityTexture(EntityBlightFire entity) {

    return TEXTURE;
  }

  public boolean shouldRender(EntityBlightFire fire, ICamera camera, double camX, double camY,
      double camZ) {

    EntityLivingBase parent = fire.getParent();
    if (parent == null)
      return false;

    AxisAlignedBB axisalignedbb = parent.getRenderBoundingBox().grow(0.5D);

    if (axisalignedbb.hasNaN() || axisalignedbb.getAverageEdgeLength() == 0.0D) {
      axisalignedbb = new AxisAlignedBB(parent.posX - 2.0D, parent.posY - 2.0D, parent.posZ - 2.0D,
          parent.posX + 2.0D, parent.posY + 2.0D, parent.posZ + 2.0D);
    }

    return parent.isInRangeToRender3d(camX, camY, camZ)
        && (parent.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(axisalignedbb));
  }

  @Override
  public void doRender(EntityBlightFire fire, double x, double y, double z, float entityYaw,
      float partialTicks) {

    EntityLivingBase parent = fire.getParent();
    if (parent == null)
      return;

    GlStateManager.disableLighting();
    GlStateManager.pushMatrix();
    GlStateManager.translate(x, y - parent.height + 0.5, z);
    float f = parent.width * FIRE_SCALE;
    GlStateManager.scale(f, f, f);
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilderSL vertexbuffer = BufferBuilderSL.INSTANCE.acquireBuffer(tessellator);

    float f1 = 0.5F;
    float f2 = 0.0F;
    float f3 = parent.height / f;
    float f4 = (float) (parent.posY - parent.getEntityBoundingBox().minY);

    GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
    GlStateManager.translate(0.0F, 0.0F, (float) ((int) f3) * 0.02F);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

    float f5 = 0.0F;
    int i = 0;

    vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
    this.bindTexture(TEXTURE);

    while (f3 > 0.0F) {
      boolean flag = i % 2 == 0;
      int frame = ClientTickHandler.ticksInGame % 32;
      float minU = flag ? 0.5f : 0.0f;
      float minV = frame / 32f;
      float maxU = flag ? 1.0f : 0.5f;
      float maxV = (frame + 1) / 32f;

      if (flag) {
        float f10 = maxU;
        maxU = minU;
        minU = f10;
      }

      vertexbuffer.pos((double) (f1 - f2), (double) (0.0F - f4), (double) f5)
          .tex((double) maxU, (double) maxV).endVertex();
      vertexbuffer.pos((double) (-f1 - f2), (double) (0.0F - f4), (double) f5)
          .tex((double) minU, (double) maxV).endVertex();
      vertexbuffer.pos((double) (-f1 - f2), (double) (1.4F - f4), (double) f5)
          .tex((double) minU, (double) minV).endVertex();
      vertexbuffer.pos((double) (f1 - f2), (double) (1.4F - f4), (double) f5)
          .tex((double) maxU, (double) minV).endVertex();
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

  public static class Factory implements IRenderFactory<EntityBlightFire> {

    public static final Factory INSTANCE = new Factory();

    @Override
    public Render createRenderFor(RenderManager manager) {

      return new RenderBlightFire(manager);
    }
  }
}
