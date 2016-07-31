package net.silentchaos512.scalinghealth.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.client.HeartDisplayHandler;
import net.silentchaos512.scalinghealth.event.ScalingHealthClientEvents;
import net.silentchaos512.scalinghealth.event.WitEventHandler;

public class ScalingHealthClientProxy extends ScalingHealthCommonProxy {

  @Override
  public void preInit(SRegistry registry) {

    super.preInit(registry);
    MinecraftForge.EVENT_BUS.register(new ScalingHealthClientEvents());
    MinecraftForge.EVENT_BUS.register(new HeartDisplayHandler());
    if (Loader.isModLoaded("wit") || Loader.isModLoaded("WIT"))
      MinecraftForge.EVENT_BUS.register(new WitEventHandler());
    registry.clientPreInit();
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
  public EntityPlayer getClientPlayer() {

    return Minecraft.getMinecraft().thePlayer;
  }
}
