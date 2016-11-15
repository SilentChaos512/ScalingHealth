package net.silentchaos512.scalinghealth.client.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.client.ClientTickHandler;
import net.silentchaos512.scalinghealth.entity.EntityBlightFire;

public class RenderBlightFire extends Render<EntityBlightFire> {

  public RenderBlightFire(RenderManager renderManager) {

    super(renderManager);
    ScalingHealth.logHelper.derp();
  }

  @Override
  protected ResourceLocation getEntityTexture(EntityBlightFire entity) {

    // TODO Auto-generated method stub
    return null;
  }

  public boolean shouldRender(EntityBlightFire fire, ICamera camera, double camX, double camY,
      double camZ) {

    // ScalingHealth.logHelper.debug("shouldRender");
    EntityLivingBase parent = fire.getParent();
    if (parent == null)
      return false;

    AxisAlignedBB axisalignedbb = parent.getRenderBoundingBox().expandXyz(0.5D);

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

    // ScalingHealth.logHelper.debug("doRender", x, y, z, "@", fire.posX, fire.posY, fire.posZ);
    EntityLivingBase parent = fire.getParent();
    if (parent == null)
      return;

    GlStateManager.disableLighting();
    TextureMap texturemap = Minecraft.getMinecraft().getTextureMapBlocks();
    TextureAtlasSprite sprite, sprite1;
    boolean vanilla = false;
    if (vanilla) {
      sprite = texturemap.getAtlasSprite("minecraft:blocks/fire_layer_0");
      sprite1 = texturemap.getAtlasSprite("minecraft:blocks/fire_layer_1");
    } else {
      sprite = texturemap.getAtlasSprite("ScalingHealth:entity/BlightFire0");
      sprite1 = texturemap.getAtlasSprite("ScalingHealth:entity/BlightFire1");
    }
    GlStateManager.pushMatrix();
    GlStateManager.translate(x, y - parent.height + 0.5, z);
    float f = parent.width * 1.6F;
    GlStateManager.scale(f, f, f);
    Tessellator tessellator = Tessellator.getInstance();
    VertexBuffer vertexbuffer = tessellator.getBuffer();
    float f1 = 0.5F;
    float f2 = 0.0F;
    float f3 = parent.height / f;
    float f4 = (float) (parent.posY - parent.getEntityBoundingBox().minY);
    GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
    GlStateManager.translate(0.0F, 0.0F, -0.3F + (float) ((int) f3) * 0.02F);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    float f5 = 0.0F;
    int i = 0;
    vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);

    while (f3 > 0.0F) {
      TextureAtlasSprite sprite2 = i % 2 == 0 ? sprite : sprite1;
      this.bindTexture(new ResourceLocation(ScalingHealth.MOD_ID_LOWER,
          i % 2 == 0 ? "textures/entity/BlightFire0.png" : "textures/entity/BlightFire1.png"));
      int frame = ClientTickHandler.ticksInGame % 32;
      float minU = 0;// sprite2.getMinU();
      float minV = frame / 32f;// sprite2.getMinV();
      float maxU = 1;// sprite2.getMaxU();
      float maxV = (frame + 1) / 32f;// sprite2.getMaxV();
      // ScalingHealth.logHelper.debug(minU, minV, maxU, maxV);

      if (i / 2 % 2 == 0) {
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

      ScalingHealth.logHelper.derp();
      return new RenderBlightFire(manager);
    }
  }
}
