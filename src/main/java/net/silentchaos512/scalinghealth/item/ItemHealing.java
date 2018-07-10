package net.silentchaos512.scalinghealth.item;

import java.util.List;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.silentchaos512.lib.item.ItemSL;
import net.silentchaos512.lib.registry.RecipeMaker;
import net.silentchaos512.lib.util.ItemHelper;
import net.silentchaos512.lib.util.LocalizationHelper;
import net.silentchaos512.lib.util.StackHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.init.ModItems;
import net.silentchaos512.scalinghealth.init.ModPotions;

public class ItemHealing extends ItemSL {

  public static enum Type {

    BANDAGE(0.3f, 1), MEDKIT(0.7f, 4);

    public final float healPercentage;
    public final int effectDuration;
    public final int amplifier;

    Type(float healPercentage, int speed) {

      this.healPercentage = healPercentage;
      this.effectDuration = (int) (healPercentage * 100 * 20 * 2 / speed);
      this.amplifier = speed - 1;
    }
  }

  public static final int USE_TIME = 5 * 20;

  public ItemHealing() {

    super(Type.values().length, ScalingHealth.MOD_ID_LOWER, "HealingItem");
    setMaxStackSize(4);
    setHasSubtypes(true);
    setCreativeTab(CreativeTabs.COMBAT);
  }

  @Override
  public void addRecipes(RecipeMaker recipes) {

    ItemStack bandages = new ItemStack(this, 1, Type.BANDAGE.ordinal());
    ItemStack medkit = new ItemStack(this, 2, Type.MEDKIT.ordinal());
    ItemStack heartDust = new ItemStack(ModItems.heartDust);
    ItemStack potion = new ItemStack(Items.POTIONITEM);
    PotionUtils.addPotionToItemStack(potion, PotionTypes.STRONG_HEALING);
    recipes.addShapedOre("bandages", bandages, "ppp", "ddd", 'p', "paper", 'd', heartDust);
    recipes.addShapedOre("medkit", medkit, "did", "bpb", "ttt", 'd', heartDust, 'i', "ingotIron",
        'b', bandages, 'p', potion, 't', new ItemStack(Blocks.STAINED_HARDENED_CLAY));
  }

  @Override
  public EnumAction getItemUseAction(ItemStack stack) {

    return EnumAction.BOW;
  }

  @Override
  public int getMaxItemUseDuration(ItemStack stack) {

    return USE_TIME;
  }

  @Override
  protected ActionResult<ItemStack> clOnItemRightClick(World world, EntityPlayer player,
      EnumHand hand) {

    ItemStack stack = player.getHeldItem(hand);
    if (player.getHealth() < player.getMaxHealth() && !player.isPotionActive(ModPotions.bandaged)) {
      player.setActiveHand(hand);
      return new ActionResult(EnumActionResult.SUCCESS, stack);
    }
    return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
  }

  @Override
  public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entityLiving) {

    if (!world.isRemote) {
      Type healingType = Type.values()[MathHelper.clamp(stack.getItemDamage(), 0,
          Type.values().length - 1)];
      entityLiving.addPotionEffect(new PotionEffect(ModPotions.bandaged, healingType.effectDuration,
          healingType.amplifier, false, false));
      StackHelper.shrink(stack, 1);

      if (entityLiving instanceof EntityPlayerMP) {
        CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP) entityLiving, stack);
      }
    }
    return stack;
  }

  @Override
  public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {

    if (count % 10 == 0) {
      if (stack.getItemDamage() == Type.MEDKIT.ordinal()
          && ScalingHealth.random.nextFloat() < 0.3f) {
        player.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 0.5f,
            (float) (0.9f + 0.025f * ScalingHealth.random.nextGaussian()));
      } else {
        player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1.25f,
            (float) (1.1f + 0.05f * ScalingHealth.random.nextGaussian()));
      }
    }
  }

  @Override
  protected void clGetSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {

    if (!ItemHelper.isInCreativeTab(item, tab)) {
      return;
    }

    for (Type type : Type.values()) {
      list.add(new ItemStack(item, 1, type.ordinal()));
    }
  }

  @Override
  public void clAddInformation(ItemStack stack, World world, List<String> list, boolean advanced) {

    LocalizationHelper loc = ScalingHealth.localizationHelper;
    Type healingType = Type.values()[MathHelper.clamp(stack.getItemDamage(), 0,
        Type.values().length - 1)];
    list.add(loc.getItemSubText("HealingItem", "healingValue",
        (int) (healingType.healPercentage * 100), healingType.effectDuration / 20));
    list.add(loc.getItemSubText("HealingItem", "howToUse", USE_TIME / 20));
  }
}
