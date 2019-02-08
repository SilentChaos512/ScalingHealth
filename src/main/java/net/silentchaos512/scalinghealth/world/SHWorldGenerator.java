/*
 * Scaling Health
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.scalinghealth.world;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class SHWorldGenerator implements IWorldGenerator {
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        // TODO
    }

//    @Override
//    public void generateSurface(World world, Random random, int posX, int posZ) {
//        int i, x, y, z, meta, veinCount, veinSize, minHeight, maxHeight;
//        BlockPos pos;
//        Block block;
//        IBlockState state;
//
//        // Crystal Shard Ore
//        block = ModBlocks.crystalOre;
//        state = block.getDefaultState();
//
//        // Vein count. Also consider bonus veins determined by distance from spawn.
//        float trueVeinCount = Config.HEART_CRYSTAL_ORE_VEIN_COUNT;
//        int spawnX = world.getSpawnPoint().getX() / 16;
//        int spawnZ = world.getSpawnPoint().getZ() / 16;
//        int chunkX = posX / 16;
//        int chunkZ = posZ / 16;
//        int deltaX = chunkX - spawnX;
//        int deltaZ = chunkZ - spawnZ;
//        float distance = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
//        trueVeinCount += Math.min(distance * Config.HEART_CRYSTAL_ORE_EXTRA_VEIN_RATE,
//                Config.HEART_CRYSTAL_ORE_EXTRA_VEIN_CAP);
//
//        veinCount = (int) trueVeinCount;
//        if (random.nextFloat() < Config.HEART_CRYSTAL_ORE_VEIN_COUNT - veinCount)
//            ++veinCount;
//
//        veinSize = Config.HEART_CRYSTAL_ORE_VEIN_SIZE;
//        minHeight = Config.HEART_CRYSTAL_ORE_MIN_HEIGHT;
//        maxHeight = Config.HEART_CRYSTAL_ORE_MAX_HEIGHT;
//
//        for (i = 0; i < veinCount; ++i) {
//            x = posX + random.nextInt(16);
//            y = random.nextInt(maxHeight - minHeight) + minHeight;
//            z = posZ + random.nextInt(16);
//            pos = new BlockPos(x, y, z);
//            new WorldGenMinable(state, veinSize).generate(world, random, pos);
//        }
//    }
//
//    @Override
//    protected void generateNether(World world, Random random, int posX, int posZ) {}
//
//    @Override
//    protected void generateEnd(World world, Random random, int posX, int posZ) {}
}
