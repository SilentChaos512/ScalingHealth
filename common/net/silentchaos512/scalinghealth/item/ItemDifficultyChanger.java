package net.silentchaos512.scalinghealth.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.silentchaos512.lib.item.ItemSL;
import net.silentchaos512.lib.util.Color;
import net.silentchaos512.lib.util.ItemHelper;
import net.silentchaos512.lib.util.StackHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.lib.EnumModParticles;
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

  @Override
  public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

    if (stack.getItemDamage() > 1) {
      return;
    }

    String amountStr = String.format("%d",
        stack.getItemDamage() == Type.ENCHANTED.ordinal()
            ? (int) ConfigScalingHealth.ENCHANTED_HEART_DIFFICULTY_CHANGE
            : (int) ConfigScalingHealth.CURSED_HEART_DIFFICULTY_CHANGE);
    if (amountStr.matches("^\\d+")) {
      amountStr = "+" + amountStr;
    }

    String line = ScalingHealth.localizationHelper.getItemSubText(itemName, "effectDesc", amountStr);
    list.add(TextFormatting.WHITE + line);
  }

  @Override
  public EnumRarity getRarity(ItemStack stack) {

    return EnumRarity.EPIC;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

    if (!ItemHelper.isInCreativeTab(this, tab))
      return;

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

    double particleX = player.posX;
    double particleY = player.posY + 0.65f * player.height;
    double particleZ = player.posZ;

    switch (Type.getByMeta(stack.getItemDamage())) {
      // Enchanted Heart
      case ENCHANTED:
        // Lower difficulty, consume 1 from stack.
        data.incrementDifficulty(ConfigScalingHealth.ENCHANTED_HEART_DIFFICULTY_CHANGE);
        StackHelper.shrink(stack, 1);

        // Particles and sound effect!
        for (int i = 0; i < 20 - 5 * ScalingHealth.proxy.getParticleSettings(); ++i) {
          double xSpeed = 0.08 * ScalingHealth.random.nextGaussian();
          double ySpeed = 0.05 * ScalingHealth.random.nextGaussian();
          double zSpeed = 0.08 * ScalingHealth.random.nextGaussian();
          ScalingHealth.proxy.spawnParticles(EnumModParticles.ENCHANTED_HEART,
              new Color(1f, 1f, 0.5f), world, particleX, particleY, particleZ, xSpeed, ySpeed,
              zSpeed);
        }
        world.playSound(null, player.getPosition(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
            SoundCategory.PLAYERS, 0.4f, 1.7f);

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
      // Cursed Heart
      case CURSED:
        // Raise difficulty, consume 1 from stack.
        data.incrementDifficulty(ConfigScalingHealth.CURSED_HEART_DIFFICULTY_CHANGE);
        StackHelper.shrink(stack, 1);

        // Particles and sound effect!
        for (int i = 0; i < 20 - 5 * ScalingHealth.proxy.getParticleSettings(); ++i) {
          double xSpeed = 0.08 * ScalingHealth.random.nextGaussian();
          double ySpeed = 0.05 * ScalingHealth.random.nextGaussian();
          double zSpeed = 0.08 * ScalingHealth.random.nextGaussian();
          ScalingHealth.proxy.spawnParticles(EnumModParticles.CURSED_HEART,
              new Color(0.4f, 0f, 0.6f), world, particleX, particleY, particleZ, xSpeed, ySpeed,
              zSpeed);
        }
        world.playSound(null, player.getPosition(), SoundEvents.BLOCK_FIRE_EXTINGUISH,
            SoundCategory.PLAYERS, 0.3f,
            (float) (0.7f + 0.05f * ScalingHealth.random.nextGaussian()));

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
      default:
        ScalingHealth.logHelper.warning("DifficultyChanger invalid meta: " + stack.getItemDamage());
        return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    }
  }
}
