package net.silentchaos512.scalinghealth.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.silentchaos512.lib.item.ItemSL;
import net.silentchaos512.lib.util.StackHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public class ItemDifficultyChanger extends ItemSL {

  static enum Type {

    ENCHANTED, CURSED, UNKNOWN; // down (0), up (1)

    static Type getByMeta(int meta) {

      if (meta < 0 || meta >= values().length)
        return UNKNOWN;
      return values()[meta];
    }
  }

  public ItemDifficultyChanger() {

    super(2, ScalingHealth.MOD_ID_LOWER, "DifficultyChanger");
    setHasSubtypes(true);
    setCreativeTab(CreativeTabs.COMBAT);
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

    list.add(new ItemStack(this, 1, 0));
    list.add(new ItemStack(this, 1, 1));
  }

  @Override
  public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

    ItemStack stack = player.getHeldItem(hand);
    PlayerData data = SHPlayerDataHandler.get(player);

    if (data == null) {
      return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    }

    switch (Type.getByMeta(stack.getItemDamage())) {
      case ENCHANTED:
        data.incrementDifficulty(ConfigScalingHealth.ENCHANTED_HEART_DIFFICULTY_CHANGE);
        StackHelper.shrink(stack, 1);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
      case CURSED:
        data.incrementDifficulty(ConfigScalingHealth.CURSED_HEART_DIFFICULTY_CHANGE);
        StackHelper.shrink(stack, 1);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
      default:
        ScalingHealth.logHelper.warning("DifficultyChanger invalid meta: " + stack.getItemDamage());
        return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    }
  }
}
