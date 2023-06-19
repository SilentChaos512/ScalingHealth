package net.silentchaos512.scalinghealth.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.silentchaos512.scalinghealth.objects.Registration;
import net.silentchaos512.scalinghealth.utils.config.EnabledFeatures;

public class PowerCrystalPlacement extends PlacementFilter {
    public static final PowerCrystalPlacement INSTANCE = new PowerCrystalPlacement();
    public static final Codec<PowerCrystalPlacement> CODEC = Codec.unit(() -> INSTANCE);

    private PowerCrystalPlacement() {}

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return EnabledFeatures.powerCrystalsOreGenEnabled();
    }

    @Override
    public PlacementModifierType<?> type() {
        return Registration.POWER_CRYSTAL_PLACEMENT.get();
    }
}
