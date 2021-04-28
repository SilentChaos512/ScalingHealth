package net.silentchaos512.scalinghealth.utils.mode;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public abstract class AreaDifficultyMode {
    private static final Map<String, Codec<? extends AreaDifficultyMode>> CODECS = Util.make(() ->
            ImmutableMap.<String, Codec<? extends AreaDifficultyMode>>builder()
                    .put("average", AreaDifficultyModes.Average.CODEC)
                    .put("extrema", AreaDifficultyModes.Extrema.CODEC)
                    .put("distance", AreaDifficultyModes.Distance.CODEC)
                    .put("distance_and_time", AreaDifficultyModes.DistanceAndTime.CODEC)
                    .put("server_wide", AreaDifficultyModes.ServerWide.CODEC)
                    .build()
    );

    public static final Codec<AreaDifficultyMode> CODEC = Codec.STRING
            .dispatch(AreaDifficultyMode::getName, CODECS::get);

    public abstract double getDifficulty(World world, BlockPos pos);

    public abstract String getName();

    public abstract static class RadialMode extends AreaDifficultyMode {
        private final int radius;

        public RadialMode(int radius) {
            this.radius = radius;
        }

        public int getRadius() {
            return radius;
        }
    }
}
