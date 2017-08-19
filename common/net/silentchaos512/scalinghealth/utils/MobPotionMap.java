package net.silentchaos512.scalinghealth.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.potion.Potion;

public class MobPotionMap {

  List<PotionEntry> list = new ArrayList<>();
  List<PotionEntry> temp = new ArrayList<>();

  public void put(Potion potion, int weight, int amplifier) {

    list.add(new PotionEntry(potion, weight, amplifier));
  }

  public PotionEntry getRandom(Random rand, int maxWeight) {

    temp.clear();
    for (PotionEntry pot : list) {
      if (pot.cost <= maxWeight) {
        temp.add(pot);
      }
    }

    if (temp.isEmpty()) {
      return null;
    }
    return temp.get(rand.nextInt(temp.size()));
  }

  public static class PotionEntry {

    public final Potion potion;
    public final int cost;
    public final int amplifier;

    public PotionEntry(Potion potion, int cost, int amplifier) {

      this.potion = potion;
      this.cost = cost;
      this.amplifier = amplifier;
    }
  }
}
