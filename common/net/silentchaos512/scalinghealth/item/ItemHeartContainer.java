package net.silentchaos512.scalinghealth.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.silentchaos512.lib.item.ItemSL;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.utils.ScalingHealthSaveStorage;

public class ItemHeartContainer extends ItemSL {

  public ItemHeartContainer() {

    super(1, ScalingHealth.MOD_ID, "HeartContainer");
    setCreativeTab(CreativeTabs.MISC);
  }

  @Override
  public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player,
      EnumHand hand) {

    if (!world.isRemote) {
      ScalingHealthSaveStorage.incrementPlayerHealth(player);
      --stack.stackSize;
    }
    return new ActionResult(EnumActionResult.SUCCESS, stack);
  }
}
