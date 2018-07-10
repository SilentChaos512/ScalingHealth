package net.silentchaos512.scalinghealth.world;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.silentchaos512.lib.world.WorldGeneratorSL;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.init.ModBlocks;

public class SHWorldGenerator extends WorldGeneratorSL {

  public SHWorldGenerator(boolean allowRetrogren) {

    super(allowRetrogren, ScalingHealth.MOD_ID_LOWER + "_retrogen");
  }

  @Override
  public void generateSurface(World world, Random random, int posX, int posZ) {

    int i, x, y, z, meta, veinCount, veinSize, minHeight, maxHeight;
    BlockPos pos;
    Block block;
    IBlockState state;

    // Crystal Shard Ore
    block = ModBlocks.crystalOre;
    state = block.getDefaultState();

    // Vein count. Also consider bonus veins determined by distance from spawn.
    float trueVeinCount = Config.HEART_CRYSTAL_ORE_VEIN_COUNT;
    int spawnX = world.getSpawnPoint().getX() / 16;
    int spawnZ = world.getSpawnPoint().getZ() / 16;
    int chunkX = posX / 16;
    int chunkZ = posZ / 16;
    int deltaX = chunkX - spawnX;
    int deltaZ = chunkZ - spawnZ;
    float distance = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    trueVeinCount += Math.min(distance * Config.HEART_CRYSTAL_ORE_EXTRA_VEIN_RATE,
        Config.HEART_CRYSTAL_ORE_EXTRA_VEIN_CAP);

    veinCount = (int) trueVeinCount;
    if (random.nextFloat() < Config.HEART_CRYSTAL_ORE_VEIN_COUNT - veinCount)
      ++veinCount;

    veinSize = Config.HEART_CRYSTAL_ORE_VEIN_SIZE;
    minHeight = Config.HEART_CRYSTAL_ORE_MIN_HEIGHT;
    maxHeight = Config.HEART_CRYSTAL_ORE_MAX_HEIGHT;

    for (i = 0; i < veinCount; ++i) {
      x = posX + random.nextInt(16);
      y = random.nextInt(maxHeight - minHeight) + minHeight;
      z = posZ + random.nextInt(16);
      pos = new BlockPos(x, y, z);
      new WorldGenMinable(state, veinSize).generate(world, random, pos);
    }
  }

  @Override
  protected void generateNether(World world, Random random, int posX, int posZ) {

  }

  @Override
  protected void generateEnd(World world, Random random, int posX, int posZ) {

  }
}
