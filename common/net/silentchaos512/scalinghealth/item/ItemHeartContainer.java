package net.silentchaos512.scalinghealth.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.silentchaos512.lib.item.ItemSL;
import net.silentchaos512.lib.util.Color;
import net.silentchaos512.lib.util.StackHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.lib.EnumModParticles;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public class ItemHeartContainer extends ItemSL {

  public ItemHeartContainer() {

    super(1, ScalingHealth.MOD_ID_LOWER, "HeartContainer");
    setCreativeTab(CreativeTabs.MISC);
  }

  @Override
  protected ActionResult<ItemStack> clOnItemRightClick(World world, EntityPlayer player, EnumHand hand) {

    ItemStack stack = player.getHeldItem(hand);

    if (!world.isRemote) {
      PlayerData data = SHPlayerDataHandler.get(player);

      boolean healthIncreaseAllowed = ConfigScalingHealth.HEARTS_INCREASE_HEALTH && data != null
          && (ConfigScalingHealth.PLAYER_HEALTH_MAX == 0 || data.getMaxHealth() < ConfigScalingHealth.PLAYER_HEALTH_MAX);

      // Heal the player (this is separate from the "healing" of the newly added heart, if that's allowed).
      boolean consumed = false;
      if (ConfigScalingHealth.HEARTS_HEALTH_RESTORED > 0 && player.getHealth() < player.getMaxHealth()) {
        float currentHealth = player.getHealth();
        player.setHealth(currentHealth + ConfigScalingHealth.HEARTS_HEALTH_RESTORED);
        consumed = true;
      }

      // End here if health increases are not allowed.
      if (!healthIncreaseAllowed) {
        if (consumed) {
          world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5f,
              1.0f + 0.1f * (float) ScalingHealth.random.nextGaussian());
          StackHelper.shrink(stack, 1);
          return new ActionResult(EnumActionResult.SUCCESS, stack);
        } else {
          return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }
      }

      // Increase health, consume heart.
      data.incrementMaxHealth(2);
      StackHelper.shrink(stack, 1);

      // Particles and sound.
      double particleX = player.posX;
      double particleY = player.posY + 0.65f * player.height;
      double particleZ = player.posZ;
      for (int i = 0; i < 40 - 10 * ScalingHealth.proxy.getParticleSettings(); ++i) {
        double xSpeed = 0.08 * ScalingHealth.random.nextGaussian();
        double ySpeed = 0.05 * ScalingHealth.random.nextGaussian();
        double zSpeed = 0.08 * ScalingHealth.random.nextGaussian();
        ScalingHealth.proxy.spawnParticles(EnumModParticles.HEART_CONTAINER,
            new Color(1f, 0f, 0f), world, particleX, particleY, particleZ, xSpeed, ySpeed,
            zSpeed);
      }
      world.playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f,
          0.7f + 0.1f * (float) ScalingHealth.random.nextGaussian());
    }
    return new ActionResult(EnumActionResult.SUCCESS, stack);
  }
}
