package net.silentchaos512.scalinghealth.init;

import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.item.ItemCrystalShard;
import net.silentchaos512.scalinghealth.item.ItemHeartContainer;

public class ModItems {

  public static ItemHeartContainer heart = new ItemHeartContainer();
  public static ItemCrystalShard crystalShard = new ItemCrystalShard();

  public static void init(SRegistry reg) {

    reg.registerItem(heart);
    reg.registerItem(crystalShard);
  }
}
