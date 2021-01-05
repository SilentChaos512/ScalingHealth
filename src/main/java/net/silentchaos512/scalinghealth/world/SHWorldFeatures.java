package net.silentchaos512.scalinghealth.world;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
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
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public class SHWorldFeatures {
    private static final List<OreSpawnInfo> ORES = ImmutableList.of(
            new OreSpawnInfo("heart_crystal_ore", ModBlocks.HEART_CRYSTAL_ORE, 6, 1, 28, EnabledFeatures::hpCrystalsOreGenEnabled),
            new OreSpawnInfo("power_crystal_ore", ModBlocks.POWER_CRYSTAL_ORE, 5, 1, 28, EnabledFeatures::powerCrystalEnabled)
    );

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void addOres(BiomeLoadingEvent event) {
        for(OreSpawnInfo info : ORES) {
            if(info.test.getAsBoolean())
                event.getGeneration().withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, info.feature.get());
        }
    }

    private static final class OreSpawnInfo {
        private final Supplier<ConfiguredFeature<?, ?>> feature;
        private final BooleanSupplier test;

        public OreSpawnInfo(String name, IBlockProvider block, int size, int count, int height, BooleanSupplier test) {
            this.feature = Suppliers.memoize(() -> Registry.register(
                    WorldGenRegistries.CONFIGURED_FEATURE,
                    ScalingHealth.getId(name),
                    Feature.ORE.withConfiguration(new OreFeatureConfig(
                            OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD,
                            block.asBlockState(),
                            size
                    )).range(height).square().func_242731_b(count)
            ));
            this.test = test;
        }
    }
}
