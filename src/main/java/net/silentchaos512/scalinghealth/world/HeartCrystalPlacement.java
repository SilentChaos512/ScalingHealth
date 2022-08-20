package net.silentchaos512.scalinghealth.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.silentchaos512.scalinghealth.utils.config.EnabledFeatures;

import java.util.Random;

import static net.silentchaos512.scalinghealth.world.WorldObjectsRegistry.HEART_CRYSTAL_PLACEMENT;

public class HeartCrystalPlacement extends PlacementFilter {
    public static final HeartCrystalPlacement INSTANCE = new HeartCrystalPlacement();
    public static final Codec<HeartCrystalPlacement> CODEC = Codec.unit(() -> INSTANCE);

    private HeartCrystalPlacement() {}

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return EnabledFeatures.hpCrystalsOreGenEnabled();
    }

    @Override
    public PlacementModifierType<?> type() {
        return Registry.PLACEMENT_MODIFIERS.get(HEART_CRYSTAL_PLACEMENT);
    }
}
