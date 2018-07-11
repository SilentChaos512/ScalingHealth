package net.silentchaos512.scalinghealth.init;

import net.minecraft.item.Item;
import net.silentchaos512.lib.registry.IRegistrationHandler;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.item.ItemDifficultyChanger;
import net.silentchaos512.scalinghealth.item.ItemHealing;
import net.silentchaos512.scalinghealth.item.ItemHeartContainer;

public class ModItems implements IRegistrationHandler<Item> {

  public static ItemHeartContainer heart = new ItemHeartContainer();
  public static Item crystalShard = new Item();
  public static Item heartDust = new Item();
  public static ItemHealing healingItem = new ItemHealing();
  public static ItemDifficultyChanger difficultyChanger = new ItemDifficultyChanger();

  @Override
  public void registerAll(SRegistry reg) {

    reg.registerItem(heart, "heartcontainer");
    reg.registerItem(crystalShard, "crystalshard");
    reg.registerItem(heartDust, "heartdust");
    reg.registerItem(healingItem, "healingitem");
    reg.registerItem(difficultyChanger, "difficultychanger");
  }
}
