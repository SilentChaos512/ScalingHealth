package net.silentchaos512.scalinghealth.event;

import java.util.Calendar;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.silentchaos512.lib.util.ChatHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.init.ModItems;
import net.silentchaos512.scalinghealth.utils.ModifierHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public class ScalingHealthCommonEvents {

  @SubscribeEvent
  public void onLivingDrops(LivingDropsEvent event) {

    // Handle heart drops.
    // Was a player responsible for the death?
    EntityPlayer player = getPlayerThatCausedDeath(event.getSource());
    if (player == null || (player instanceof FakePlayer
        && !ConfigScalingHealth.FAKE_PLAYERS_CAN_GENERATE_HEARTS)) {
      return;
    }

    EntityLivingBase killedEntity = event.getEntityLiving();
    if (!killedEntity.world.isRemote) {
      Random rand = ScalingHealth.random;
      int stackSize = 0;

      // Different drop rates for hostiles and passives.
      float dropRate = killedEntity instanceof IMob ? ConfigScalingHealth.HEART_DROP_CHANCE_HOSTILE
          : ConfigScalingHealth.HEART_DROP_CHANCE_PASSIVE;
      if (killedEntity instanceof EntitySlime) {
        dropRate /= 6f;
      }

      // Basic heart drops for all mobs.
      if (event.isRecentlyHit() && rand.nextFloat() <= dropRate) {
        stackSize += 1;
      }

      // Heart drops for bosses.
      if (!killedEntity.isNonBoss()) {
        int min = ConfigScalingHealth.HEARTS_DROPPED_BY_BOSS_MIN;
        int max = ConfigScalingHealth.HEARTS_DROPPED_BY_BOSS_MAX;
        stackSize += min + rand.nextInt(max - min + 1);
      }

      if (stackSize > 0) {
        Item itemToDrop = ConfigScalingHealth.HEART_DROP_SHARDS_INSTEAD ? ModItems.crystalShard
            : ModItems.heart;
        killedEntity.dropItem(itemToDrop, stackSize);
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onXPDropped(LivingExperienceDropEvent event) {

    EntityLivingBase entityLiving = event.getEntityLiving();

    // Additional XP from all mobs.
    short difficulty = entityLiving.getEntityData()
        .getShort(DifficultyHandler.NBT_ENTITY_DIFFICULTY);
    float multi = 1.0f + ConfigScalingHealth.MOB_XP_BOOST * difficulty;

    float amount = event.getDroppedExperience();
    amount *= multi;

    // Additional XP from blights.
    if (BlightHandler.isBlight(entityLiving)) {
      amount *= ConfigScalingHealth.BLIGHT_XP_MULTIPLIER;
    }

    event.setDroppedExperience(Math.round(amount));
  }

  /**
   * Get the player that caused a mob's death. Could be a FakePlayer or null.
   * 
   * @return The player that caused the damage, or the owner of the tamed animal that caused the damage.
   */
  private @Nullable EntityPlayer getPlayerThatCausedDeath(DamageSource source) {

    if (source == null) {
      return null;
    }

    // Player is true source.
    Entity entitySource = source.getTrueSource();
    if (entitySource instanceof EntityPlayer) {
      return (EntityPlayer) entitySource;
    }

    // Player's pet is true source.
    boolean isTamedAnimal = entitySource instanceof EntityTameable
        && ((EntityTameable) entitySource).isTamed();
    if (entitySource instanceof EntityTameable) {
      EntityTameable tamed = (EntityTameable) entitySource;
      if (tamed.isTamed() && tamed.getOwner() instanceof EntityPlayer) {
        return (EntityPlayer) tamed.getOwner();
      }
    }

    // No player responsible.
    return null;
  }

  @SubscribeEvent
  public void onPlayerRespawn(
      net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent event) {

    // Set player health correctly after respawn.
    if (event.player instanceof EntityPlayerMP) {
      EntityPlayerMP player = (EntityPlayerMP) event.player;
      PlayerData data = SHPlayerDataHandler.get(player);

      // Lose health on death?
      if (ConfigScalingHealth.PLAYER_HEALTH_LOST_ON_DEATH > 0) {
        float newHealth = data.getMaxHealth() - ConfigScalingHealth.PLAYER_HEALTH_LOST_ON_DEATH;
        float startHealth = ConfigScalingHealth.PLAYER_STARTING_HEALTH;
        data.setMaxHealth(newHealth < startHealth ? startHealth : newHealth);
      }

      // Lose difficulty on death?
      double currentDifficulty = data.getDifficulty();
      double newDifficulty = MathHelper.clamp(
          currentDifficulty - ConfigScalingHealth.DIFFICULTY_LOST_ON_DEATH,
          ConfigScalingHealth.DIFFICULTY_MIN, ConfigScalingHealth.DIFFICULTY_MAX);
      data.setDifficulty(newDifficulty);

      // Apply health modifier
      float health = player.getHealth();
      float maxHealth = data.getMaxHealth();
      ModifierHandler.setMaxHealth(player, maxHealth, 0);
      if (health != maxHealth && maxHealth > 0)
        player.setHealth(maxHealth);
    }
  }

  @SubscribeEvent
  public void onPlayerJoinedServer(PlayerLoggedInEvent event) {

    // Sync player data and set health.
    if (event.player instanceof EntityPlayerMP) {
      EntityPlayerMP player = (EntityPlayerMP) event.player;
      PlayerData data = SHPlayerDataHandler.get(player);

      // Resets, based on config?
      Calendar today = Calendar.getInstance();
      Calendar lastTimePlayed = data.getLastTimePlayed();

      if (ConfigScalingHealth.DIFFFICULTY_RESET_TIME.shouldReset(today, lastTimePlayed)) {
        ScalingHealth.logHelper.info(String.format("Reset player %s's difficulty to %d",
            player.getName(), (int) ConfigScalingHealth.DIFFICULTY_DEFAULT));
        ChatHelper.sendMessage(player, "[Scaling Health] Your difficulty has been reset.");
        data.setDifficulty(ConfigScalingHealth.DIFFICULTY_DEFAULT);
      }
      if (ConfigScalingHealth.PLAYER_HEALTH_RESET_TIME.shouldReset(today, lastTimePlayed)) {
        data.setMaxHealth(ConfigScalingHealth.PLAYER_STARTING_HEALTH);
        ScalingHealth.logHelper.info(String.format("Reset player %s's health to %d",
            player.getName(), (int) ConfigScalingHealth.PLAYER_STARTING_HEALTH));
        ChatHelper.sendMessage(player, "[Scaling Health] Your health has been reset.");
      }

      data.getLastTimePlayed().setTime(today.getTime());

      // Apply health modifier
      float health = player.getHealth();
      float maxHealth = data.getMaxHealth();
      ModifierHandler.setMaxHealth(player, maxHealth, 0);
      if (health > maxHealth && maxHealth > 0)
        player.setHealth(maxHealth);
    }
  }

  @SubscribeEvent
  public void onPlayerSleepInBed(PlayerSleepInBedEvent event) {

    if (!event.getEntityPlayer().world.isRemote && event.getResultStatus() == SleepResult.OK
        && ConfigScalingHealth.WARN_WHEN_SLEEPING
        && ConfigScalingHealth.DIFFICULTY_FOR_SLEEPING > 0f) {
      ChatHelper.sendStatusMessage(event.getEntityPlayer(),
          TextFormatting.RED + ScalingHealth.localizationHelper.getMiscText("sleepWarning"), false);
    }
  }

  @SubscribeEvent
  public void onPlayerWakeUp(PlayerWakeUpEvent event) {

    ScalingHealth.logHelper.debug(event.getEntityPlayer().world.isRemote, event.updateWorld(),
        event.shouldSetSpawn());
    if (!event.getEntityPlayer().world.isRemote && !event.updateWorld()) {
      EntityPlayer player = event.getEntityPlayer();
      PlayerData data = SHPlayerDataHandler.get(player);
      if (data != null) {
        data.incrementDifficulty(ConfigScalingHealth.DIFFICULTY_FOR_SLEEPING);
      }
    }
  }

  @SubscribeEvent
  public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {

    if (event.getModID().equals(ScalingHealth.MOD_ID_LOWER)) {
      ConfigScalingHealth.INSTANCE.load();
      ConfigScalingHealth.INSTANCE.save();
    }
  }
}
