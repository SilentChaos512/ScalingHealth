package net.silentchaos512.scalinghealth.datagen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraftforge.common.data.JsonCodecProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.objects.Registration;
import net.silentchaos512.scalinghealth.world.HeartCrystalPlacement;
import net.silentchaos512.scalinghealth.world.PowerCrystalPlacement;

import java.util.List;
import java.util.Map;

import static net.minecraft.data.worldgen.features.OreFeatures.*;
import static net.silentchaos512.scalinghealth.objects.Registration.*;
import static net.silentchaos512.scalinghealth.world.WorldObjectsRegistry.*;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID ,bus = Mod.EventBusSubscriber.Bus.MOD)
public class GenEvent {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        generator.addProvider(true, new Recipes(generator));
        generator.addProvider(true, new LootTablesGenerator(generator));
        generator.addProvider(true, new SHEntityTagsProvider(event));
        generator.addProvider(true, new SHBlockTagsProvider(event));
        generator.addProvider(true, new LootModifierGen(event.getGenerator()));
        generator.addProvider(true, new EnglishLocalization(event.getGenerator()));
        generator.addProvider(true, new BlockStateGen(event));

        //World Generation
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.builtinCopy());
        HolderSet.Named<Biome> overworld = new HolderSet.Named<>(ops.registry(Registry.BIOME_REGISTRY).get(), BiomeTags.IS_OVERWORLD);

        var heartCrystalStoneFeature = ore(HEART_CRYSTAL_ORE.get(), STONE_ORE_REPLACEABLES, 4);
        var heartCrystalDeepslateFeature = ore(DEEPSLATE_POWER_CRYSTAL_ORE.get(), DEEPSLATE_ORE_REPLACEABLES, 5);
        var powerCrystalStoneFeature = ore(POWER_CRYSTAL_ORE.get(), STONE_ORE_REPLACEABLES, 3);
        var powerCrystalDeepslateFeature = ore(DEEPSLATE_POWER_CRYSTAL_ORE.get(), DEEPSLATE_ORE_REPLACEABLES, 4);

        var heartCrystalStoneName = ScalingHealth.getId("heart_crystal_stone_ore");
        var heartCrystalDeepslateName = ScalingHealth.getId("heart_crystal_stone_deepslate");
        var powerCrystalStoneName = ScalingHealth.getId("power_crystal_stone_ore");
        var powerCrystalDeepslateName = ScalingHealth.getId("power_crystal_stone_deepslate");

        Map<ResourceLocation, ConfiguredFeature<? , ?>> oreFeatures = ImmutableMap.of(
                heartCrystalStoneName, heartCrystalStoneFeature,
                heartCrystalDeepslateName, heartCrystalDeepslateFeature,
                powerCrystalStoneName, powerCrystalStoneFeature,
                powerCrystalDeepslateName, powerCrystalDeepslateFeature
        );

        DataProvider configuredFeatureProvider = JsonCodecProvider.forDatapackRegistry(event.getGenerator(), event.getExistingFileHelper(), ScalingHealth.MOD_ID, ops, Registry.CONFIGURED_FEATURE_REGISTRY,  oreFeatures);

        var heartCrystalStonePlaced = placed(holder(heartCrystalStoneFeature, ops, heartCrystalStoneName), HeartCrystalPlacement.INSTANCE, 100, 1);
        var heartCrystalDeepslatePlaced = placed(holder(heartCrystalDeepslateFeature, ops, heartCrystalDeepslateName), HeartCrystalPlacement.INSTANCE, 64, 1);
        var powerCrystalStonePlaced = placed(holder(powerCrystalStoneFeature, ops, powerCrystalStoneName), PowerCrystalPlacement.INSTANCE, 100, 1);
        var powerCrystalDeepslatePlaced = placed( holder(powerCrystalDeepslateFeature, ops, powerCrystalDeepslateName), PowerCrystalPlacement.INSTANCE, 64, 1);

        Map<ResourceLocation, PlacedFeature> orePlacedFeatures = ImmutableMap.of(
                heartCrystalStoneName, heartCrystalStonePlaced,
                heartCrystalDeepslateName, heartCrystalDeepslatePlaced,
                powerCrystalStoneName, powerCrystalStonePlaced,
                powerCrystalDeepslateName, powerCrystalDeepslatePlaced
        );

        var heartCrystalStone = holderPlaced(heartCrystalStonePlaced, ops, heartCrystalStoneName);
        var heartCrystalDeepslate = holderPlaced(heartCrystalDeepslatePlaced, ops, heartCrystalDeepslateName);
        var powerCrystalStone = holderPlaced(powerCrystalStonePlaced, ops, powerCrystalStoneName);
        var powerCrystalDeepslate = holderPlaced(powerCrystalDeepslatePlaced, ops, powerCrystalDeepslateName);

        DataProvider placedFeatureProvider = JsonCodecProvider.forDatapackRegistry(event.getGenerator(), event.getExistingFileHelper(), ScalingHealth.MOD_ID, ops, Registry.PLACED_FEATURE_REGISTRY, orePlacedFeatures);

        BiomeModifier ores = new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworld,
                HolderSet.direct(heartCrystalStone, heartCrystalDeepslate, powerCrystalStone, powerCrystalDeepslate),
                GenerationStep.Decoration.UNDERGROUND_ORES
        );

        DataProvider biomeModifierProvider = JsonCodecProvider.forDatapackRegistry(event.getGenerator(), event.getExistingFileHelper(), ScalingHealth.MOD_ID, ops, ForgeRegistries.Keys.BIOME_MODIFIERS,
                ImmutableMap.of(ScalingHealth.getId("sh_ores"), ores));

        generator.addProvider(true, configuredFeatureProvider);
        generator.addProvider(true, placedFeatureProvider);
        generator.addProvider(true, biomeModifierProvider);
    }

    public static PlacedFeature placed(Holder<ConfiguredFeature<? , ?>> feature,  PlacementModifier config, int height, int count) {
        return new PlacedFeature(feature, placements(height, count, config));
    }

    public static List<PlacementModifier> placements(int height, int count, PlacementModifier config) {
        return ImmutableList.of(
                HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(height)),
                InSquarePlacement.spread(),
                CountPlacement.of(count),
                config
        );
    }

    public static ConfiguredFeature<?, ?> ore(Block block, RuleTest replacing, int size) {
        return new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(replacing, block.defaultBlockState(), size));
    }

    public static Holder<ConfiguredFeature<? ,? >> holder(ConfiguredFeature<? ,? > feature, RegistryOps<JsonElement> ops, ResourceLocation location) {
        return ops.registryAccess.registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY).getOrCreateHolderOrThrow(ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, location));
    }

    public static Holder<PlacedFeature> holderPlaced(PlacedFeature feature, RegistryOps<JsonElement> ops, ResourceLocation location) {
        return ops.registryAccess.registryOrThrow(Registry.PLACED_FEATURE_REGISTRY).getOrCreateHolderOrThrow(ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, location));
    }
}
