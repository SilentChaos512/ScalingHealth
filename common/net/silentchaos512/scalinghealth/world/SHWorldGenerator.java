package net.silentchaos512.scalinghealth.world;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.init.ModBlocks;

public class SHWorldGenerator implements IWorldGenerator {

  @Override
  public void generate(Random random, int chunkX, int chunkZ, World world,
      IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {

    switch (world.provider.getDimension()) {
      case -1: break;
      case  1: break;
      default: generateSurface(world, random, chunkX * 16, chunkZ * 16);
    }
  }

  private void generateSurface(World world, Random random, int posX, int posZ) {

    int i, x, y, z, meta, veinCount, veinSize, minHeight, maxHeight;
    BlockPos pos;
    Block block;
    IBlockState state;

    // Crystal Shard Ore
    block = ModBlocks.crystalOre;
    state = block.getDefaultState();

    veinCount = (int) ConfigScalingHealth.HEART_CRYSTAL_ORE_VEIN_COUNT;
    if (random.nextFloat() < ConfigScalingHealth.HEART_CRYSTAL_ORE_VEIN_COUNT - veinCount)
      ++veinCount;
    veinSize = ConfigScalingHealth.HEART_CRYSTAL_ORE_VEIN_SIZE;
    minHeight = ConfigScalingHealth.HEART_CRYSTAL_ORE_MIN_HEIGHT;
    maxHeight = ConfigScalingHealth.HEART_CRYSTAL_ORE_MAX_HEIGHT;

    for (i = 0; i < veinCount; ++i) {
      x = posX + random.nextInt(16);
      y = random.nextInt(maxHeight - minHeight) + minHeight;
      z = posZ + random.nextInt(16);
      pos = new BlockPos(x, y, z);
      new WorldGenMinable(state, veinSize).generate(world, random, pos);
    }
  }
}
