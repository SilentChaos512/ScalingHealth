package net.silentchaos512.scalinghealth.proxy;

import org.apache.commons.lang3.NotImplementedException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.lib.util.Color;
import net.silentchaos512.scalinghealth.client.ClientTickHandler;
import net.silentchaos512.scalinghealth.client.DifficultyDisplayHandler;
import net.silentchaos512.scalinghealth.client.HeartDisplayHandler;
import net.silentchaos512.scalinghealth.client.key.KeyTrackerSH;
import net.silentchaos512.scalinghealth.client.render.particle.ParticleSH;
import net.silentchaos512.scalinghealth.event.ScalingHealthClientEvents;
import net.silentchaos512.scalinghealth.event.WitEventHandler;
import net.silentchaos512.scalinghealth.init.ModEntities;
import net.silentchaos512.scalinghealth.lib.EnumModParticles;

public class ScalingHealthClientProxy extends ScalingHealthCommonProxy {

  @Override
  public void preInit(SRegistry registry) {

    super.preInit(registry);
    MinecraftForge.EVENT_BUS.register(new ScalingHealthClientEvents());
    MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
    MinecraftForge.EVENT_BUS.register(new HeartDisplayHandler());
    MinecraftForge.EVENT_BUS.register(DifficultyDisplayHandler.INSTANCE);
    MinecraftForge.EVENT_BUS.register(KeyTrackerSH.INSTANCE);
    if (Loader.isModLoaded("wit") || Loader.isModLoaded("WIT"))
      MinecraftForge.EVENT_BUS.register(new WitEventHandler());
    registry.clientPreInit();

    ModEntities.registerRenderers(registry);
  }

  @Override
  public void init(SRegistry registry) {

    super.init(registry);
    registry.clientInit();
  }

  @Override
  public void postInit(SRegistry registry) {

    super.postInit(registry);
    registry.clientPostInit();
  }

  @Override
  public void spawnParticles(EnumModParticles type, Color color, World world, double x, double y,
      double z, double motionX, double motionY, double motionZ) {

    Particle fx = null;

    float r = color.getRed();
    float g = color.getGreen();
    float b = color.getBlue();

    switch (type) {
      case CURSED_HEART:
      case ENCHANTED_HEART:
      case HEART_CONTAINER:
        fx = new ParticleSH(world, x, y, z, motionX, motionY, motionZ, 1.0f, 10, r, g, b);
        break;
      default:
        throw new NotImplementedException("Unknown particle type: " + type);
    }

    if (fx != null) {
      Minecraft.getMinecraft().effectRenderer.addEffect(fx);
    }
  }

  @Override
  public EntityPlayer getClientPlayer() {

    return Minecraft.getMinecraft().player;
  }

  @Override
  public int getParticleSettings() {

    return Minecraft.getMinecraft().gameSettings.particleSetting;
  }
}
