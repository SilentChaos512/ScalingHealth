package net.silentchaos512.scalinghealth.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.silentchaos512.lib.item.ItemSL;
import net.silentchaos512.lib.registry.RecipeMaker;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.init.ModItems;

public class ItemHeartDust extends ItemSL {

  public ItemHeartDust() {

    super(1, ScalingHealth.MOD_ID_LOWER, "HeartDust");
    setCreativeTab(CreativeTabs.MATERIALS);
  }

  @Override
  public void addRecipes(RecipeMaker recipes) {

    recipes.addShapeless("heart_dust", new ItemStack(this, 24), new ItemStack(ModItems.heart));
  }
}
