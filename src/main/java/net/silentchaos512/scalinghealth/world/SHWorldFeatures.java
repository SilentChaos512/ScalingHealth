package net.silentchaos512.scalinghealth.world;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.objects.Registration;
import net.silentchaos512.scalinghealth.utils.config.EnabledFeatures;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public class SHWorldFeatures {
    private static final List<OreSpawnInfo> ORES = ImmutableList.of(
            new OreSpawnInfo("heart_crystal_ore", Registration.HEART_CRYSTAL_ORE, 4, 1,
                    OreFeatures.STONE_ORE_REPLACEABLES, 100, EnabledFeatures::hpCrystalsOreGenEnabled),
            new OreSpawnInfo("power_crystal_ore", Registration.POWER_CRYSTAL_ORE, 3, 1,
                    OreFeatures.STONE_ORE_REPLACEABLES, 100, EnabledFeatures::powerCrystalsOreGenEnabled),
            new OreSpawnInfo("deepslate_heart_crystal_ore", Registration.DEEPLSATE_HEART_CRYSTAL_ORE, 6, 1,
                    OreFeatures.DEEPSLATE_ORE_REPLACEABLES, 64, EnabledFeatures::hpCrystalsOreGenEnabled),
            new OreSpawnInfo("deepslate_power_crystal_ore", Registration.DEEPSLATE_POWER_CRYSTAL_ORE, 5, 1,
                    OreFeatures.DEEPSLATE_ORE_REPLACEABLES, 64, EnabledFeatures::powerCrystalsOreGenEnabled)
    );

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void addOres(BiomeLoadingEvent event) {
        for(OreSpawnInfo info : ORES) {
            if(info.test.getAsBoolean())
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, info.feature.get());
        }
    }

    private static final class OreSpawnInfo {
        private final Supplier<PlacedFeature> feature;
        private final BooleanSupplier test;

        public OreSpawnInfo(String name, Supplier<Block> block, int size, int count, RuleTest replacing, int height, BooleanSupplier test) {
            this.feature = Suppliers.memoize(() -> {
                ConfiguredFeature<? ,? > f = Registry.register(
                    BuiltinRegistries.CONFIGURED_FEATURE,
                    ScalingHealth.getId(name),
                    Feature.ORE.configured(new OreConfiguration(
                            replacing,
                            block.get().defaultBlockState(),
                            size
                    )));
                return Registry.register(BuiltinRegistries.PLACED_FEATURE,
                        ScalingHealth.getId(name),
                        f.placed(
                                HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(height)),
                                InSquarePlacement.spread(),
                                CountPlacement.of(count)));
            });
            this.test = test;
        }
    }
}
