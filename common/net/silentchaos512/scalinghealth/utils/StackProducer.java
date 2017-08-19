package net.silentchaos512.scalinghealth.utils;

import java.util.Random;

import net.minecraft.item.ItemStack;
import net.silentchaos512.lib.util.StackHelper;

/**
 * Used by EquipmentTierMap. The default StackProducer is just a wrapper for an ItemStack. Override the get method to
 * allow the production of random ItemStacks, or whatever you need.
 * 
 * @author Silent
 *
 */
public class StackProducer {

  public ItemStack stack;

  protected StackProducer() {

    this(StackHelper.empty());
  }

  public StackProducer(ItemStack stack) {

    this.stack = stack;
  }

  // Override as needed.
  public ItemStack get(Random rand) {

    return stack;
  }
}
