package net.silentchaos512.scalinghealth.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.silentchaos512.lib.util.StackHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;

public class EquipmentTierMap {

  public final int tierCount;
  public final EntityEquipmentSlot slot;

  List<List<StackProducer>> sets;

  public EquipmentTierMap(int tierCount, EntityEquipmentSlot slot) {

    this.tierCount = tierCount;
    this.slot = slot;

    sets = new ArrayList<>();
    for (int i = 0; i < tierCount; ++i) {
      sets.add(new ArrayList<>());
    }
  }

  public void put(ItemStack stack, int tier) {

    put(new StackProducer(stack), tier);
  }

  public void put(StackProducer producer, int tier) {

    if (tier < 0 || tier >= tierCount) {
      throw new IllegalArgumentException("tier must be between 0 and " + tierCount);
    }

    // TODO: We could also check the stack is valid for the slot.

    sets.get(tier).add(producer);
  }

  public ItemStack getRandom(int tier) {

    if (tier < 0 || tier >= tierCount) {
      throw new IllegalArgumentException("tier must be between 0 and " + tierCount);
    }

    List<StackProducer> list = sets.get(tier);
    if (list.isEmpty()) {
      return StackHelper.empty();
    }

    Random rand = ScalingHealth.random;
    return list.get(rand.nextInt(list.size())).get(rand);
  }

  public ItemStack get(int tier, int index) {

    if (tier < 0 || tier >= tierCount) {
      throw new IllegalArgumentException("tier must be between 0 and " + tierCount);
    }

    List<StackProducer> list = sets.get(tier);
    if (list.isEmpty()) {
      return StackHelper.empty();
    }
    if (index < 0 || index >= list.size()) {
      throw new IllegalArgumentException("index must be between 0 and " + list.size());
    }

    return list.get(index).get(ScalingHealth.random);
  }
}
