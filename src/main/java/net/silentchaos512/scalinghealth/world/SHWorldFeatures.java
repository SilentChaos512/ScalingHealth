package net.silentchaos512.scalinghealth.world;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.lib.block.IBlockProvider;
import net.silentchaos512.scalinghealth.init.ModBlocks;

public class SHWorldFeatures {
    public static void addFeaturesToBiomes() {
        for (Biome biome : ForgeRegistries.BIOMES) {
            addOre(biome, ModBlocks.HEART_CRYSTAL_ORE, 6, 1, 0, 28);
            addOre(biome, ModBlocks.POWER_CRYSTAL_ORE, 6, 1, 0, 28);
        }
    }

    private static void addOre(Biome biome, IBlockProvider block, int size, int count, int minHeight, int maxHeight) {
        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Biome.createDecoratedFeature(
                Feature.ORE,
                new OreFeatureConfig(
                        OreFeatureConfig.FillerBlockType.NATURAL_STONE,
                        block.asBlockState(),
                        size
                ),
                Placement.COUNT_RANGE,
                new CountRangeConfig(count, minHeight, 0, maxHeight)
        ));
    }
}
