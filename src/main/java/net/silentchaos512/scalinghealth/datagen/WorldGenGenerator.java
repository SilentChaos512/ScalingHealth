package net.silentchaos512.scalinghealth.datagen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.world.HeartCrystalPlacement;
import net.silentchaos512.scalinghealth.world.PowerCrystalPlacement;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static net.silentchaos512.scalinghealth.objects.Registration.*;
import static net.silentchaos512.scalinghealth.objects.Registration.DEEPSLATE_POWER_CRYSTAL_ORE;

public class WorldGenGenerator extends DatapackBuiltinEntriesProvider {
    private static final RuleTest replaceStone = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
    private static final RuleTest replaceDeepslate = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

    private static final ConfiguredFeature<?, ?> heartCrystalStoneFeature = ore(HEART_CRYSTAL_ORE.get(), replaceStone, 4);
    private static final ConfiguredFeature<?, ?> heartCrystalDeepslateFeature = ore(DEEPSLATE_POWER_CRYSTAL_ORE.get(), replaceDeepslate, 5);
    private static final ConfiguredFeature<?, ?> powerCrystalStoneFeature = ore(POWER_CRYSTAL_ORE.get(), replaceStone, 3);
    private static final ConfiguredFeature<?, ?> powerCrystalDeepslateFeature = ore(DEEPSLATE_POWER_CRYSTAL_ORE.get(), replaceDeepslate, 4);

    private static final ResourceKey<ConfiguredFeature<?, ?>> heartCrystalStoneName = configuredFeature(ScalingHealth.getId("heart_crystal_stone_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> heartCrystalDeepslateName = configuredFeature(ScalingHealth.getId("heart_crystal_stone_deepslate"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> powerCrystalStoneName = configuredFeature(ScalingHealth.getId("power_crystal_stone_ore"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> powerCrystalDeepslateName = configuredFeature(ScalingHealth.getId("power_crystal_stone_deepslate"));

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ctx -> {
                ctx.register(heartCrystalStoneName, heartCrystalStoneFeature);
                ctx.register(heartCrystalDeepslateName, heartCrystalDeepslateFeature);
                ctx.register(powerCrystalStoneName, powerCrystalStoneFeature);
                ctx.register(powerCrystalDeepslateName, powerCrystalDeepslateFeature);
            })
            .add(Registries.PLACED_FEATURE, ctx -> {
                var heartCrystalStonePlaced = placed(holderFeature(ctx, heartCrystalStoneName), HeartCrystalPlacement.INSTANCE, 100, 1);
                var heartCrystalDeepslatePlaced = placed(holderFeature(ctx, heartCrystalDeepslateName), HeartCrystalPlacement.INSTANCE, 64, 1);
                var powerCrystalStonePlaced = placed(holderFeature(ctx, powerCrystalStoneName), PowerCrystalPlacement.INSTANCE, 100, 1);
                var powerCrystalDeepslatePlaced = placed( holderFeature(ctx, powerCrystalDeepslateName), PowerCrystalPlacement.INSTANCE, 64, 1);

                ctx.register(placedFeature(heartCrystalStoneName.location()), heartCrystalStonePlaced);
                ctx.register(placedFeature(heartCrystalDeepslateName.location()), heartCrystalDeepslatePlaced);
                ctx.register(placedFeature(powerCrystalStoneName.location()), powerCrystalStonePlaced);
                ctx.register(placedFeature(powerCrystalDeepslateName.location()), powerCrystalDeepslatePlaced);
            })
            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, ctx -> {
                var heartCrystalStone = holderPlaced(ctx, heartCrystalStoneName.location());
                var heartCrystalDeepslate = holderPlaced(ctx, heartCrystalDeepslateName.location());
                var powerCrystalStone = holderPlaced(ctx, powerCrystalStoneName.location());
                var powerCrystalDeepslate = holderPlaced(ctx, powerCrystalDeepslateName.location());
                HolderSet.Named<Biome> isOverworldTag = ctx.lookup(Registries.BIOME).getOrThrow(BiomeTags.IS_OVERWORLD);

                BiomeModifier ores = new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        isOverworldTag,
                        HolderSet.direct(heartCrystalStone, heartCrystalDeepslate, powerCrystalStone, powerCrystalDeepslate),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                );

                ctx.register(biomeModifier(ScalingHealth.getId("sh_ores")), ores);
            });

    public WorldGenGenerator(GatherDataEvent event) {
        super(event.getGenerator().getPackOutput(), event.getLookupProvider(), BUILDER, Set.of(ScalingHealth.MOD_ID));
    }

    public static ConfiguredFeature<?, ?> ore(Block block, RuleTest replacing, int size) {
        return new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(replacing, block.defaultBlockState(), size));
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> configuredFeature(ResourceLocation name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, name);
    }

    protected static ResourceKey<PlacedFeature> placedFeature(ResourceLocation name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, name);
    }

    protected static ResourceKey<BiomeModifier> biomeModifier(ResourceLocation name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, name);
    }

    public static PlacedFeature placed(Holder<ConfiguredFeature<? , ?>> feature, PlacementModifier config, int height, int count) {
        return new PlacedFeature(feature, placements(height, count, config));
    }

    public static List<PlacementModifier> placements(int height, int count, PlacementModifier config) {
        return ImmutableList.of(
                HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(height)),
                InSquarePlacement.spread(),
                CountPlacement.of(count),
                BiomeFilter.biome(),
                config
        );
    }

    public static Holder<ConfiguredFeature<? ,? >> holderFeature(BootstapContext<PlacedFeature> ctx, ResourceKey<ConfiguredFeature<?, ?>> location) {
        return ctx.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(location);
    }

    public static Holder<PlacedFeature> holderPlaced(BootstapContext<BiomeModifier> ctx, ResourceLocation location) {
        return ctx.lookup(Registries.PLACED_FEATURE).getOrThrow(placedFeature(location));
    }
}
