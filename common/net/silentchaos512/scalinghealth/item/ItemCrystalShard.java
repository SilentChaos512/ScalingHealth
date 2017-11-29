package net.silentchaos512.scalinghealth.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.silentchaos512.lib.item.ItemSL;
import net.silentchaos512.lib.registry.RecipeMaker;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.init.ModItems;

public class ItemCrystalShard extends ItemSL {

  public ItemCrystalShard() {

    super(1, ScalingHealth.MOD_ID_LOWER, "CrystalShard");
  }

  @Override
  public void addRecipes(RecipeMaker recipes) {

    recipes.addShaped("heart_container", new ItemStack(ModItems.heart), "sss", "sss", "sss", 's', new ItemStack(this));
  }
}
