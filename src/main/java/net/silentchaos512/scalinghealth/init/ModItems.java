package net.silentchaos512.scalinghealth.init;

import net.minecraft.item.Item;
import net.silentchaos512.lib.registry.IRegistrationHandler;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.item.ItemCrystalShard;
import net.silentchaos512.scalinghealth.item.ItemDifficultyChanger;
import net.silentchaos512.scalinghealth.item.ItemHealing;
import net.silentchaos512.scalinghealth.item.ItemHeartContainer;
import net.silentchaos512.scalinghealth.item.ItemHeartDust;

public class ModItems implements IRegistrationHandler<Item> {

  public static ItemHeartContainer heart = new ItemHeartContainer();
  public static ItemCrystalShard crystalShard = new ItemCrystalShard();
  public static ItemHeartDust heartDust = new ItemHeartDust();
  public static ItemHealing healingItem = new ItemHealing();
  public static ItemDifficultyChanger difficultyChanger = new ItemDifficultyChanger();

  @Override
  public void registerAll(SRegistry reg) {

    reg.registerItem(heart);
    reg.registerItem(crystalShard);
    reg.registerItem(heartDust);
    reg.registerItem(healingItem);
    reg.registerItem(difficultyChanger);
  }
}
