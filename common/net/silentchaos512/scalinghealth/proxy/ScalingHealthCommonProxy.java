package net.silentchaos512.scalinghealth.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.silentchaos512.lib.proxy.CommonProxy;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.event.ScalingHealthCommonEvents;

public class ScalingHealthCommonProxy extends CommonProxy {

  @Override
  public void preInit(SRegistry registry) {

    super.preInit(registry);
    MinecraftForge.EVENT_BUS.register(new ScalingHealthCommonEvents());
  }

  public EntityPlayer getClientPlayer() {

    return null;
  }
}
