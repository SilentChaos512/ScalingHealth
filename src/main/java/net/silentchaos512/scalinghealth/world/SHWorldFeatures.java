package net.silentchaos512.scalinghealth.world;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silentchaos512.lib.block.IBlockProvider;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.init.ModBlocks;
import net.silentchaos512.scalinghealth.utils.EnabledFeatures;

import java.util.List;
import java.util.function.BooleanSupplier;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public class SHWorldFeatures {
    private static final List<OreSpawnInfo> ORES = ImmutableList.of(
            new OreSpawnInfo(ModBlocks.HEART_CRYSTAL_ORE, 6, 1, 28, EnabledFeatures::hpCrystalsOreGenEnabled),
            new OreSpawnInfo(ModBlocks.POWER_CRYSTAL_ORE, 5, 1, 28, EnabledFeatures::powerCrystalEnabled)
    );

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void addOres(BiomeLoadingEvent event)
    {
        for(OreSpawnInfo info : ORES)
        {
            if(!info.test.getAsBoolean())
                continue;
            event.getGeneration().withFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
                    Feature.ORE.withConfiguration(new OreFeatureConfig(
                            OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD,
                            info.block.asBlockState(),
                            info.size
                    )).range(info.height).square().func_242731_b(info.count)
            );
        }
    }

    private static final class OreSpawnInfo {
        private final IBlockProvider block;
        private final int size;
        private final int count;
        private final int height;
        private final BooleanSupplier test;

        public OreSpawnInfo(IBlockProvider block, int size, int count, int height, BooleanSupplier test) {
            this.block = block;
            this.size = size;
            this.count = count;
            this.height = height;
            this.test = test;
        }
    }
}
