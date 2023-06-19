package net.silentchaos512.scalinghealth.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.silentchaos512.scalinghealth.objects.Registration;
import net.silentchaos512.scalinghealth.utils.config.EnabledFeatures;

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
        return Registration.HEART_CRYSTAL_PLACEMENT.get();
    }
}
