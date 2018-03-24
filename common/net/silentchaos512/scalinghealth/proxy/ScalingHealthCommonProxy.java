package net.silentchaos512.scalinghealth.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.silentchaos512.lib.proxy.CommonProxy;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.lib.util.Color;
import net.silentchaos512.scalinghealth.compat.morpheus.SHMorpheusCompat;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.event.BlightHandler;
import net.silentchaos512.scalinghealth.event.DifficultyHandler;
import net.silentchaos512.scalinghealth.event.PetEventHandler;
import net.silentchaos512.scalinghealth.event.PlayerBonusRegenHandler;
import net.silentchaos512.scalinghealth.event.ScalingHealthCommonEvents;
import net.silentchaos512.scalinghealth.init.ModEntities;
import net.silentchaos512.scalinghealth.lib.EnumModParticles;
import net.silentchaos512.scalinghealth.network.NetworkHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;

public class ScalingHealthCommonProxy extends CommonProxy {

  @Override
  public void preInit(SRegistry registry) {

    super.preInit(registry);

    ModEntities.init(registry);
    DifficultyHandler.INSTANCE.initDefaultEquipment();

    NetworkHandler.init();

    MinecraftForge.EVENT_BUS.register(new ScalingHealthCommonEvents());
    MinecraftForge.EVENT_BUS.register(new SHPlayerDataHandler.EventHandler());
    MinecraftForge.EVENT_BUS.register(PlayerBonusRegenHandler.INSTANCE);
    MinecraftForge.EVENT_BUS.register(DifficultyHandler.INSTANCE);
    MinecraftForge.EVENT_BUS.register(BlightHandler.INSTANCE);
    MinecraftForge.EVENT_BUS.register(PetEventHandler.INSTANCE);

    // Morpheus compat
    if (Loader.isModLoaded("morpheus") && ConfigScalingHealth.MORPHEUS_OVERRIDE)
      SHMorpheusCompat.init();
  }

  @Override
  public void init(SRegistry registry) {

    super.init(registry);

    DifficultyHandler.INSTANCE.initPotionMap();
  }

  public void spawnParticles(EnumModParticles type, Color color, World world, double x, double y,
      double z, double motionX, double motionY, double motionZ) {

  }

  public EntityPlayer getClientPlayer() {

    return null;
  }

  public int getParticleSettings() {

    return 0;
  }
}
