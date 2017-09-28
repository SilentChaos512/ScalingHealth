package net.silentchaos512.scalinghealth.event;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.utils.ModifierHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public class PlayerBonusRegenHandler {

  public static PlayerBonusRegenHandler INSTANCE = new PlayerBonusRegenHandler();

  private Map<String, Integer> timers = new HashMap<>();

  public int getTimerForPlayer(EntityPlayer player) {

    if (player == null || !timers.containsKey(player.getName()))
      return -1;
    return timers.get(player.getName());
  }

  @SubscribeEvent
  public void onPlayerTick(PlayerTickEvent event) {

    if (event.side == Side.CLIENT || !ConfigScalingHealth.ENABLE_BONUS_HEALTH_REGEN)
      return;

    EntityPlayer player = event.player;
    String name = player.getName();

    // Add player timer if needed.
    if (!timers.containsKey(name))
      timers.put(name, ConfigScalingHealth.BONUS_HEALTH_REGEN_INITIAL_DELAY);

    int foodLevel = player.getFoodStats().getFoodLevel();
    boolean foodLevelOk = foodLevel >= ConfigScalingHealth.BONUS_HEALTH_REGEN_MIN_FOOD
        && foodLevel <= ConfigScalingHealth.BONUS_HEALTH_REGEN_MAX_FOOD;

    if (player.getHealth() < player.getMaxHealth() && foodLevelOk) {
      // Tick timer, heal player and reset on 0.
      int timer = timers.get(name);
      if (--timer <= 0) {
        player.heal(1f);
        player.addExhaustion(0.025f);
        timer = ConfigScalingHealth.BONUS_HEALTH_REGEN_DELAY;
      }
      timers.put(name, timer);
    }
  }

  @SubscribeEvent
  public void onPlayerHurt(LivingHurtEvent event) {

    EntityLivingBase entityLiving = event.getEntityLiving();
    if (entityLiving.world.isRemote || !(entityLiving instanceof EntityPlayer))
      return;
    timers.put(entityLiving.getName(), ConfigScalingHealth.BONUS_HEALTH_REGEN_INITIAL_DELAY);
  }
}
