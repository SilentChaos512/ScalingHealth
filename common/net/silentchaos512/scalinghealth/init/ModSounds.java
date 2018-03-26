package net.silentchaos512.scalinghealth.init;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.silentchaos512.lib.registry.IRegistrationHandler;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.ScalingHealth;

public class ModSounds implements IRegistrationHandler<SoundEvent> {

  public static final SoundEvent PLAYER_DIED = create("player_died");

  @Override
  public void registerAll(SRegistry reg) {

    reg.registerSoundEvent(PLAYER_DIED, "player_died");
  }

  private static SoundEvent create(String soundId) {

    ResourceLocation name = new ResourceLocation(ScalingHealth.MOD_ID_LOWER, soundId);
    return new SoundEvent(name);
  }
}
