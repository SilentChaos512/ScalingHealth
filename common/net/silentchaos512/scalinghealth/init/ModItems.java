package net.silentchaos512.scalinghealth.init;

import net.minecraft.item.Item;
import net.silentchaos512.lib.registry.IRegistrationHandler;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.item.ItemCrystalShard;
import net.silentchaos512.scalinghealth.item.ItemHeartContainer;

public class ModItems implements IRegistrationHandler<Item> {

  public static ItemHeartContainer heart = new ItemHeartContainer();
  public static ItemCrystalShard crystalShard = new ItemCrystalShard();

  @Override
  public void registerAll(SRegistry reg) {

    reg.registerItem(heart);
    reg.registerItem(crystalShard);
  }
}
