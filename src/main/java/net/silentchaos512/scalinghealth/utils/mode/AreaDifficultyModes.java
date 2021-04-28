package net.silentchaos512.scalinghealth.utils.mode;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.capability.DifficultySourceCapability;
import net.silentchaos512.scalinghealth.capability.IDifficultySource;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;
import net.silentchaos512.scalinghealth.utils.serialization.SerializationUtils;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

public class AreaDifficultyModes {
    public static class Average extends AreaDifficultyMode.RadialMode {
        public static final Codec<Average> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        SerializationUtils.positiveInt(64).fieldOf("radius").forGetter(RadialMode::getRadius),
                        Codec.BOOL.optionalFieldOf("weighted", true).forGetter(a -> a.weighted)
                ).apply(inst, Average::new)
        );

        public final boolean weighted;

        public Average(int radius, boolean weighted) {
            super(radius);
            this.weighted = weighted;
        }

        @Override
        public double getDifficulty(World world, BlockPos pos) {
            return this.weighted ? getWeightedAverage(world, pos) : getAverage(world, pos);
        }

        public double getAverage(World world, BlockPos pos) {
            List<Pair<IDifficultySource, BlockPos>> sources = SHDifficulty.positionedPlayerSources(world, pos, getRadius());
            return sources.stream()
                    .map(p -> p.getFirst().getDifficulty())
                    .reduce(Float::sum)
                    .orElse(0F) / sources.size();
        }

        public double getWeightedAverage(World world, BlockPos pos) {
            double total = 0;
            int totalWeight = 0;
            int rSq = getRadius() * getRadius();
            for (Pair<IDifficultySource, BlockPos> p : SHDifficulty.positionedPlayerSources(world, pos, getRadius())) {
                int distanceSq = (int) pos.distanceSq(p.getSecond());
                int weight = 1 - distanceSq / rSq;
                total += weight * p.getFirst().getDifficulty();
                totalWeight += weight;
            }
            return total / totalWeight;
        }

        @Override
        public String getName() {
            return "average";
        }
    }

    public static class Extrema extends AreaDifficultyMode.RadialMode {
        public static final Codec<Extrema> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        SerializationUtils.positiveInt(64).fieldOf("radius").forGetter(RadialMode::getRadius),
                        Codec.BOOL.optionalFieldOf("min", true).forGetter(a -> a.min)
                ).apply(inst, Extrema::new)
        );

        private final BinaryOperator<Double> extremaFct;
        private final boolean min;

        public Extrema(int radius, boolean min) {
            super(radius);
            this.min = min;
            this.extremaFct = min ? Math::min : Math::max;
        }

        @Override
        public double getDifficulty(World world, BlockPos pos) {
            double extrema = 0;
            for (Tuple<BlockPos, IDifficultySource> tuple : SHDifficulty.allPlayerSources(world, pos, getRadius())) {
                extrema = extremaFct.apply((double)tuple.getB().getDifficulty(), extrema);
            }
            return extrema;
        }

        @Override
        public String getName() {
            return "extrema";
        }
    }

    public static class Distance extends AreaDifficultyMode {
        public static final Codec<Distance> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        SerializationUtils.positiveDouble().fieldOf("distanceFactor").forGetter(r -> r.distanceFactor),
                        Codec.BOOL.optionalFieldOf("fromOrigin", false).forGetter(a -> a.fromOrigin)
                ).apply(inst, Distance::new)
        );

        private static final BlockPos ADJUSTED_ZERO = BlockPos.ZERO.up(65);

        private final boolean fromOrigin;
        private final double distanceFactor;
        private final Supplier<BlockPos> center;
        private BlockPos worldSpawn;

        public Distance(double distanceFactor, boolean fromOrigin) {
            this.distanceFactor = distanceFactor;
            this.fromOrigin = fromOrigin;
            this.center = fromOrigin ? () -> ADJUSTED_ZERO : () -> this.worldSpawn;
        }

        @Override
        public double getDifficulty(World world, BlockPos pos) {
            if(worldSpawn == null)
                worldSpawn = new BlockPos(world.getWorldInfo().getSpawnX(), world.getWorldInfo().getSpawnY(), world.getWorldInfo().getSpawnZ());
            return Math.sqrt(pos.distanceSq(center.get())) * this.distanceFactor;
        }

        @Override
        public String getName() {
            return "distance";
        }
    }

    public static class DistanceAndTime extends AreaDifficultyMode {
        public static final Codec<DistanceAndTime> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Average.CODEC.fieldOf("average").forGetter(dt -> dt.time),
                        Distance.CODEC.fieldOf("distance").forGetter(dt -> dt.distance)
                ).apply(inst, DistanceAndTime::new)
        );

        private final Average time;
        private final Distance distance;

        public DistanceAndTime(Average time, Distance distance) {
            this.time = time;
            this.distance = distance;
        }

        @Override
        public double getDifficulty(World world, BlockPos pos) {
            return time.getDifficulty(world, pos) + distance.getDifficulty(world, pos);
        }

        @Override
        public String getName() {
            return "distance_and_time";
        }
    }

    public static class ServerWide extends AreaDifficultyMode {
        public static final ServerWide INSTANCE = new ServerWide();

        public static final Codec<ServerWide> CODEC = Codec.unit(INSTANCE);

        @Override
        public double getDifficulty(World world, BlockPos pos) {
            return DifficultySourceCapability.getOverworldCap()
                    .map(IDifficultySource::getDifficulty).orElse(0f);
        }

        @Override
        public String getName() {
            return "server_wide";
        }
    }
}
