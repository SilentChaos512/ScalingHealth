package net.silentchaos512.scalinghealth.world;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.objects.Registration;

import java.util.List;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WorldObjectsRegistry {
    public static Holder<PlacedFeature> HEART_CRYSTAL_STONE;
    public static Holder<PlacedFeature> HEART_CRYSTAL_DEEPSLATE;
    public static Holder<PlacedFeature> POWER_CRYSTAL_STONE;
    public static Holder<PlacedFeature> POWER_CRYSTAL_DEEPSLATE;
    public static PlacementModifierType<HeartCrystalPlacement> HEART_CRYSTAL_PLACEMENT;
    public static PlacementModifierType<PowerCrystalPlacement> POWER_CRYSTAL_PLACEMENT;

    @SubscribeEvent
    public static void register(RegistryEvent<Feature<?>> event) {
        HEART_CRYSTAL_STONE = placedFeature(ScalingHealth.getId("heart_crystal_ore"), Registration.HEART_CRYSTAL_ORE.get(),
                HeartCrystalPlacement.INSTANCE, OreFeatures.STONE_ORE_REPLACEABLES, 4, 100, 1);
        HEART_CRYSTAL_DEEPSLATE = placedFeature(ScalingHealth.getId("deepslate_heart_crystal_ore"), Registration.DEEPLSATE_HEART_CRYSTAL_ORE.get(),
                HeartCrystalPlacement.INSTANCE, OreFeatures.DEEPSLATE_ORE_REPLACEABLES, 5, 64, 1);
        POWER_CRYSTAL_STONE = placedFeature(ScalingHealth.getId("power_crystal_ore"), Registration.POWER_CRYSTAL_ORE.get(),
                PowerCrystalPlacement.INSTANCE, OreFeatures.STONE_ORE_REPLACEABLES, 3, 100, 1);
        POWER_CRYSTAL_DEEPSLATE = placedFeature(ScalingHealth.getId("deepslate_power_crystal_ore"), Registration.DEEPSLATE_POWER_CRYSTAL_ORE.get(),
                PowerCrystalPlacement.INSTANCE, OreFeatures.DEEPSLATE_ORE_REPLACEABLES, 5, 64, 1);
        HEART_CRYSTAL_PLACEMENT = register(ScalingHealth.getId("heart_crystal_placement"), HeartCrystalPlacement.CODEC);
        POWER_CRYSTAL_PLACEMENT = register(ScalingHealth.getId("power_crystal_placement"), PowerCrystalPlacement.CODEC);
    }

    public static Holder<PlacedFeature> placedFeature(ResourceLocation loc, Block ore, PlacementModifier config, RuleTest replacing, int size, int height, int count) {
        return PlacementUtils.register(loc.toString(), feature(loc, ore, replacing, size), placements(height, count, config));
    }

    public static Holder<ConfiguredFeature<OreConfiguration , ?>> feature(ResourceLocation loc, Block block, RuleTest replacing, int size) {
        return FeatureUtils.register(loc.toString(), Feature.ORE, new OreConfiguration(replacing, block.defaultBlockState(), size));
    }

    public static List<PlacementModifier> placements(int height, int count, PlacementModifier config) {
        return ImmutableList.of(
                HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(height)),
                InSquarePlacement.spread(),
                CountPlacement.of(count),
                config
        );
    }

    private static <P extends PlacementModifier> PlacementModifierType<P> register(ResourceLocation loc, Codec<P> codec) {
        return Registry.register(Registry.PLACEMENT_MODIFIERS, loc, () -> codec);
    }
}
