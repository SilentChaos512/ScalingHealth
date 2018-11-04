package net.silentchaos512.scalinghealth.utils;

import net.minecraft.item.ItemStack;

import java.util.Random;

/**
 * Used by EquipmentTierMap. The default StackProducer is just a wrapper for an ItemStack. Override
 * the get method to allow the production of random ItemStacks, or whatever you need.
 *
 * @author Silent
 * @deprecated Remove in 1.13, will likely replace with a {@link java.util.function.Supplier} or
 * some type of {@link java.util.function.Function}
 */
@Deprecated
public class StackProducer {
    public ItemStack stack;

    protected StackProducer() {
        this(ItemStack.EMPTY);
    }

    public StackProducer(ItemStack stack) {
        this.stack = stack;
    }

    // Override as needed.
    public ItemStack get(Random rand) {
        return stack;
    }
}
