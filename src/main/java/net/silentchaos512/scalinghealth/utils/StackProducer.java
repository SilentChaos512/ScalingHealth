package net.silentchaos512.scalinghealth.utils;

import net.minecraft.item.ItemStack;
import net.silentchaos512.lib.util.StackHelper;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Used by EquipmentTierMap. The default StackProducer is just a wrapper for an ItemStack. Override the get method to
 * allow the production of random ItemStacks, or whatever you need.
 *
 * @author Silent
 * @deprecated Use {@link Supplier <ItemStack>}
 */
@Deprecated
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
