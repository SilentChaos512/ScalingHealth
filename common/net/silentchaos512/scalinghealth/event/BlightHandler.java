package net.silentchaos512.scalinghealth.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.silentchaos512.lib.util.PlayerHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.init.ModItems;

public class BlightHandler {

  public static final String NBT_BLIGHT = ScalingHealth.MOD_ID_OLD + ".IsBlight";

  public static int UPDATE_DELAY = 200;
  public static int UPDATE_DELAY_SALT = 5 + ScalingHealth.random.nextInt(10);

  public static BlightHandler INSTANCE = new BlightHandler();

  public static boolean isBlight(EntityLivingBase entityLiving) {

    return entityLiving != null && entityLiving.getEntityData() != null
        && entityLiving.getEntityData().getBoolean(NBT_BLIGHT);
  }

  public static void markBlight(EntityLivingBase entityLiving) {

    if (entityLiving == null || entityLiving.getEntityData() == null)
      return;
    entityLiving.getEntityData().setBoolean(NBT_BLIGHT, true);
  }

  // **********
  // * Events *
  // **********

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onBlightKilled(LivingDeathEvent event) {

    if (event.getSource() == null || !isBlight(event.getEntityLiving()))
      return;

    if (event.getSource().getEntity() instanceof EntityPlayer) {
      EntityLivingBase blight = event.getEntityLiving();
      EntityPlayer player = (EntityPlayer) event.getSource().getEntity();

      // Tell all players that the blight was killed.
      String message = ScalingHealth.localizationHelper.getLocalizedString("blight",
          "killedByPlayer", blight.getName(), player.getName());
      ScalingHealth.logHelper.info(message);
      for (EntityPlayer p : player.worldObj.getPlayers(EntityPlayer.class, e -> true))
        PlayerHelper.addChatMessage(p, message);

      // Drop hearts!
      int heartCount = ScalingHealth.random.nextInt(ConfigScalingHealth.HEARTS_DROPPED_BY_BLIGHT_MAX
          - ConfigScalingHealth.HEARTS_DROPPED_BY_BLIGHT_MIN + 1)
          + ConfigScalingHealth.HEARTS_DROPPED_BY_BLIGHT_MIN;
      if (heartCount > 0)
        event.getEntityLiving().dropItem(ModItems.heart, heartCount);
    }
  }

  @SubscribeEvent
  public void onBlightUpdate(LivingUpdateEvent event) {

    if (event.getEntityLiving() != null) {
      EntityLivingBase entityLiving = event.getEntityLiving();
      if ((entityLiving.worldObj.getTotalWorldTime() + UPDATE_DELAY_SALT) % UPDATE_DELAY == 0
          && isBlight(entityLiving)) {
        entityLiving.setFire(Integer.MAX_VALUE / 20);
      }
    }
  }
}
