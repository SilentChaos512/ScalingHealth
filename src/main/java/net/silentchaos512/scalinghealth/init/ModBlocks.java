package net.silentchaos512.scalinghealth.init;

import net.minecraft.block.Block;
import net.silentchaos512.lib.registry.IRegistrationHandler;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.block.BlockCrystalOre;

public class ModBlocks implements IRegistrationHandler<Block> {

  public static BlockCrystalOre crystalOre = new BlockCrystalOre();

  @Override
  public void registerAll(SRegistry reg) {

    reg.registerBlock(crystalOre);
  }
}
