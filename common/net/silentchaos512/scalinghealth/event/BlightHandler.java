package net.silentchaos512.scalinghealth.event;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.silentchaos512.lib.util.LocalizationHelper;
import net.silentchaos512.lib.util.PlayerHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.entity.EntityBlightFire;
import net.silentchaos512.scalinghealth.init.ModItems;
import net.silentchaos512.scalinghealth.network.NetworkHandler;
import net.silentchaos512.scalinghealth.network.message.MessageMarkBlight;

public class BlightHandler {

  public static final String NBT_BLIGHT = ScalingHealth.MOD_ID_OLD + ".IsBlight";

  public static int UPDATE_DELAY = 200;
  public static int UPDATE_DELAY_SALT = 5 + ScalingHealth.random.nextInt(10);

  public static BlightHandler INSTANCE = new BlightHandler();

  // ******************
  // * Blight marking *
  // ******************

  public static boolean isBlight(EntityLivingBase entityLiving) {

    return entityLiving != null && entityLiving.getEntityData() != null
        && (entityLiving.getEntityData().getBoolean(NBT_BLIGHT));
  }

  public static void markBlight(EntityLivingBase entityLiving) {

    if (entityLiving == null || entityLiving.getEntityData() == null)
      return;
    entityLiving.getEntityData().setBoolean(NBT_BLIGHT, true);
  }

  public static void spawnBlightFire(EntityLivingBase blight) {

    if (blight.worldObj.isRemote)
      return;

    EntityBlightFire fire = new EntityBlightFire(blight);
    fire.setPosition(blight.posX, blight.posY, blight.posZ);
    blight.worldObj.spawnEntityInWorld(fire);
    if (ConfigScalingHealth.BLIGHT_FIRE_RIDES_BLIGHT)
      fire.startRiding(blight);
  }

  public static EntityBlightFire getBlightFire(EntityLivingBase blight) {

    World world = blight.worldObj;
    List<EntityBlightFire> fireList = world.getEntities(EntityBlightFire.class, e -> true);

    for (EntityBlightFire fire : fireList)
      if (fire.getParent() != null && fire.getParent().equals(blight))
        return fire;

    return null;
  }

  // **********
  // * Events *
  // **********

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onBlightKilled(LivingDeathEvent event) {

    if (event.getSource() == null || !isBlight(event.getEntityLiving())
        || event.getEntity().worldObj.isRemote)
      return;

    LocalizationHelper loc = ScalingHealth.localizationHelper;

    if (event.getSource().getEntity() instanceof EntityPlayer) {
      // Killed by a player.
      EntityLivingBase blight = event.getEntityLiving();
      EntityPlayer player = (EntityPlayer) event.getSource().getEntity();

      // Tell all players that the blight was killed.
      String message = loc.getLocalizedString("blight", "killedByPlayer", blight.getName(),
          player.getName());
      ScalingHealth.logHelper.info(message);
      for (EntityPlayer p : player.worldObj.getPlayers(EntityPlayer.class, e -> true))
        PlayerHelper.addChatMessage(p, message);

      // Drop hearts!
      int heartCount = ScalingHealth.random.nextInt(ConfigScalingHealth.HEARTS_DROPPED_BY_BLIGHT_MAX
          - ConfigScalingHealth.HEARTS_DROPPED_BY_BLIGHT_MIN + 1)
          + ConfigScalingHealth.HEARTS_DROPPED_BY_BLIGHT_MIN;
      if (heartCount > 0)
        blight.dropItem(ModItems.heart, heartCount);
    } else {
      // Killed by something else.
      EntityLivingBase blight = event.getEntityLiving();

      // Tell all players that the blight died.
      String message = event.getSource().getDeathMessage(blight).getFormattedText();
      String blightName = loc.getLocalizedString("blight", "name", blight.getName());
      message = message.replaceFirst(blight.getName(), blightName);
      ScalingHealth.logHelper.info(message);
      for (EntityPlayer p : blight.worldObj.getPlayers(EntityPlayer.class, e -> true))
        PlayerHelper.addChatMessage(p, message);
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onXPDropped(LivingExperienceDropEvent event) {

    if (!isBlight(event.getEntityLiving()))
      return;

    int amount = event.getDroppedExperience();
    amount *= ConfigScalingHealth.BLIGHT_XP_MULTIPLIER;
    event.setDroppedExperience(amount);
  }

  @SubscribeEvent
  public void onBlightUpdate(LivingUpdateEvent event) {

    if (event.getEntityLiving() != null) {
      EntityLivingBase entityLiving = event.getEntityLiving();
      World world = entityLiving.worldObj;

      // Blights only!
      if (!isBlight(entityLiving))
        return;

      boolean updateTime = (world.getTotalWorldTime() + UPDATE_DELAY_SALT) % UPDATE_DELAY == 0;

      // Update packets to make sure clients know this entity is a blight.
      if (updateTime && !world.isRemote) {
        // Send message to clients to make sure they know the entity is a blight.
        MessageMarkBlight message = new MessageMarkBlight(entityLiving);
        NetworkHandler.INSTANCE.sendToAllAround(message, new TargetPoint(entityLiving.dimension,
            entityLiving.posX, entityLiving.posY, entityLiving.posZ, 128));

        // Effects
        // Assign a blight fire if necessary.
        if (getBlightFire(entityLiving) == null)
          spawnBlightFire(entityLiving);
      }
    }
  }
}
