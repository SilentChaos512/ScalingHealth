package net.silentchaos512.scalinghealth.init;

import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.block.BlockCrystalOre;

public class ModBlocks {

  public static BlockCrystalOre crystalOre = new BlockCrystalOre();

  public static void init(SRegistry reg) {

    reg.registerBlock(crystalOre);
  }
}
