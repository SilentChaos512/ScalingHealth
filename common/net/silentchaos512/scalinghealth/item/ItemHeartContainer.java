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
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public class ItemHeartContainer extends ItemSL {

  public ItemHeartContainer() {

    super(1, ScalingHealth.MOD_ID_LOWER, "HeartContainer");
    setCreativeTab(CreativeTabs.MISC);
  }

  @Override
  public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

    ItemStack stack = player.getHeldItem(hand);

    if (!world.isRemote) {
      PlayerData data = SHPlayerDataHandler.get(player);

      if (data == null || (data.getMaxHealth() >= ConfigScalingHealth.PLAYER_HEALTH_MAX
          && ConfigScalingHealth.PLAYER_HEALTH_MAX > 0)) {
        return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
      }

      data.incrementMaxHealth(2);
      stack.shrink(1);
      world.playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
          SoundCategory.PLAYERS, 1.0f, 0.7f + 0.1f * (float) ScalingHealth.random.nextGaussian());
    }
    return new ActionResult(EnumActionResult.SUCCESS, stack);
  }
}
