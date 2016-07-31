package net.silentchaos512.scalinghealth.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.client.HeartDisplayHandler;
import net.silentchaos512.scalinghealth.event.ScalingHealthClientEvents;

public class ScalingHealthClientProxy extends ScalingHealthCommonProxy {

  @Override
  public void preInit(SRegistry registry) {

    super.preInit(registry);
    MinecraftForge.EVENT_BUS.register(new ScalingHealthClientEvents());
    MinecraftForge.EVENT_BUS.register(new HeartDisplayHandler());
  }

  @Override
  public EntityPlayer getClientPlayer() {

    return Minecraft.getMinecraft().thePlayer;
  }
}
