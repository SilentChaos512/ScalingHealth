package net.silentchaos512.scalinghealth.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.silentchaos512.scalinghealth.config.Config;

public class PetEventHandler {

  public static PetEventHandler INSTANCE = new PetEventHandler();

  @SubscribeEvent
  public void onLivingUpdate(LivingUpdateEvent event) {

    final int regenDelay = Config.PET_REGEN_DELAY;
    if (regenDelay <= 0) {
      return;
    }

    EntityLivingBase entity = event.getEntityLiving();
    if (entity != null && !entity.world.isRemote) {
      boolean isTamed = entity instanceof EntityTameable && ((EntityTameable) entity).isTamed();
      boolean isRegenTime = entity.hurtResistantTime <= 0 && entity.ticksExisted % regenDelay == 0;
      if (isTamed && isRegenTime) {
        entity.heal(2f);
      }
    }
  }
}
