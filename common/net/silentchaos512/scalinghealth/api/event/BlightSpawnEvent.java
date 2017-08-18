package net.silentchaos512.scalinghealth.api.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;

public class BlightSpawnEvent extends LivingSpawnEvent {

  public BlightSpawnEvent(EntityLiving entity, World world, float x, float y, float z) {

    super(entity, world, x, y, z);
  }
}
