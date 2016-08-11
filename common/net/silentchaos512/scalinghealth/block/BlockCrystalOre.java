package net.silentchaos512.scalinghealth.block;

import java.util.Random;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.silentchaos512.lib.block.BlockSL;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.init.ModItems;

public class BlockCrystalOre extends BlockSL {

  public BlockCrystalOre() {

    super(1, ScalingHealth.MOD_ID_LOWER, "CrystalOre", Material.ROCK);

    setHardness(3.0f);
    setResistance(15.0f);
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 2);
  }

  @Override
  public void addRecipes() {

    // TODO: Smetling? Sag Mill?
  }

  @Override
  public Item getItemDropped(IBlockState state, Random rand, int fortune) {

    return ModItems.crystalShard;
  }

  @Override
  public int damageDropped(IBlockState state) {

    // TODO
    return 0;
  }

  @Override
  public int quantityDroppedWithBonus(int fortune, Random rand) {

    if (fortune > 0) {
      int j = rand.nextInt(fortune) - 1;
      if (j < 0)
        j = 0;
      return quantityDropped(rand) * (j + 1);
    } else {
      return quantityDropped(rand);
    }
  }

  @Override
  public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {

    Item item = getItemDropped(world.getBlockState(pos), RANDOM, fortune);
    if (item != Item.getItemFromBlock(this)) {
      return 1 + RANDOM.nextInt(5);
    }
    return 0;
  }
}
