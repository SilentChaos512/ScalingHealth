package net.silentchaos512.scalinghealth.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.silentchaos512.lib.proxy.CommonProxy;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.event.BlightHandler;
import net.silentchaos512.scalinghealth.event.DifficultyHandler;
import net.silentchaos512.scalinghealth.event.PlayerBonusRegenHandler;
import net.silentchaos512.scalinghealth.event.ScalingHealthCommonEvents;
import net.silentchaos512.scalinghealth.init.ModEntities;
import net.silentchaos512.scalinghealth.network.NetworkHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;

public class ScalingHealthCommonProxy extends CommonProxy {

  @Override
  public void preInit(SRegistry registry) {

    super.preInit(registry);

    ModEntities.init(registry);

    NetworkHandler.init();

    MinecraftForge.EVENT_BUS.register(new ScalingHealthCommonEvents());
    MinecraftForge.EVENT_BUS.register(new SHPlayerDataHandler.EventHandler());
    MinecraftForge.EVENT_BUS.register(PlayerBonusRegenHandler.INSTANCE);
    MinecraftForge.EVENT_BUS.register(DifficultyHandler.INSTANCE);
    MinecraftForge.EVENT_BUS.register(BlightHandler.INSTANCE);
  }

  public EntityPlayer getClientPlayer() {

    return null;
  }

  public int getParticleSettings() {

    return 0;
  }
}
