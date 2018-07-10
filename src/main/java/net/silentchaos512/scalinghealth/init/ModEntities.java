package net.silentchaos512.scalinghealth.init;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.client.render.entity.RenderBlightFire;
import net.silentchaos512.scalinghealth.entity.EntityBlightFire;

public class ModEntities {

  public static void init(SRegistry reg) {

    reg.registerEntity(EntityBlightFire.class, "BlightFire", 64, 20, true);
  }

  public static void registerRenderers(SRegistry reg) {

    reg.registerEntityRenderer(EntityBlightFire.class, RenderBlightFire.Factory.INSTANCE);
  }
}
