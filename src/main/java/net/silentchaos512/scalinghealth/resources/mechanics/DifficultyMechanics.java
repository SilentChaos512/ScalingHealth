package net.silentchaos512.scalinghealth.resources.mechanics;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.udojava.evalex.Expression;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.scalinghealth.utils.mode.AreaDifficultyMode;
import net.silentchaos512.scalinghealth.utils.mode.AreaDifficultyModes;
import net.silentchaos512.scalinghealth.utils.serialization.SerializationUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DifficultyMechanics {
    public static final String FILE = "difficulty";

    public static final Codec<DifficultyMechanics> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    SerializationUtils.numberConstraintCodec(
                          Codec.DOUBLE, "startingValue",
                          Codec.DOUBLE, "minValue",
                          Codec.DOUBLE, "maxValue"
                    ).forGetter(d -> new SerializationUtils.NumberConstraint<>(d.starting, d.minValue, d.maxValue)),
                    Codec.DOUBLE.fieldOf("changePerSecond").forGetter(d -> d.changePerSecond),
                    AreaDifficultyMode.CODEC.fieldOf("mode").forGetter(d -> d.mode),
                    SerializationUtils.EXPRESSION_CODEC.fieldOf("groupBonus").forGetter(d -> d.groupBonus),
                    SerializationUtils.positiveInt().fieldOf("groupBonusRadius").forGetter(d -> d.groupBonusRadius),
                    Codec.DOUBLE.fieldOf("idleMultiplier").forGetter(d -> d.idleMultiplier),
                    Codec.BOOL.fieldOf("afkMessage").forGetter(d -> d.afkMessage),
                    Codec.INT.fieldOf("timeBeforeAfk").forGetter(d -> d.timeBeforeAfk),
                    Codec.BOOL.fieldOf("sleepWarningMessage").forGetter(d -> d.sleepWarningMessage),
                    Multipliers.CODEC.fieldOf("multipliers").forGetter(d -> d.multipliers),
                    Mutators.CODEC.fieldOf("mutators").forGetter(d -> d.mutators)
            ).apply(inst, DifficultyMechanics::new)
    );

    public final double starting;
    public final double minValue;
    public final double maxValue;
    public final double changePerSecond;
    public final AreaDifficultyMode mode;
    public final Supplier<Expression> groupBonus;
    public final int groupBonusRadius;
    public final double idleMultiplier;
    public final boolean afkMessage;
    public final int timeBeforeAfk;
    public final boolean sleepWarningMessage;
    public final Multipliers multipliers;
    public final Mutators mutators;

    private static final Supplier<Expression> DEFAULT_EXPRESSION = Suppliers.memoize(() -> new Expression("difficulty"));
    private static final Mutators DEFAULT_MUTATORS = new Mutators(
            DEFAULT_EXPRESSION,
            DEFAULT_EXPRESSION,
            DEFAULT_EXPRESSION,
            DEFAULT_EXPRESSION,
            DEFAULT_EXPRESSION,
            DEFAULT_EXPRESSION,
            Collections.emptyList()
    );

    private static final Multipliers DEFAULT_MULTIPLIERS = new Multipliers(
            Collections.emptyList(),
            Collections.emptyList()
    );

    public static final DifficultyMechanics DEFAULT = new DifficultyMechanics(
            new SerializationUtils.NumberConstraint<>(0D, 0D, 250D),
            0.0011575,
            new AreaDifficultyModes.DistanceAndTime(new AreaDifficultyModes.Average(256, true), new AreaDifficultyModes.Distance(0.0025, false)),
            () -> new Expression("1 + 0.05 * (areaPlayerCount - 1)"),
            128,
            0.5,
            true,
            120,
            true,
            DEFAULT_MULTIPLIERS,
            DEFAULT_MUTATORS
    );

    public DifficultyMechanics(SerializationUtils.NumberConstraint<Double, Double, Double> nc, double changePerSecond, AreaDifficultyMode mode, Supplier<Expression> groupBonus, int groupBonusRadius, double idleMultiplier, boolean afkMessage, int timeBeforeAfk, boolean sleepWarningMessage, Multipliers multipliers, Mutators mutators) {
        this.starting = nc.starting;
        this.minValue = nc.min;
        this.maxValue = nc.max == 0 ? Integer.MAX_VALUE : nc.max;
        this.changePerSecond = changePerSecond;
        this.mode = mode;
        this.groupBonus = groupBonus;
        this.groupBonusRadius = groupBonusRadius;
        this.idleMultiplier = idleMultiplier;
        this.afkMessage = afkMessage;
        this.timeBeforeAfk = timeBeforeAfk;
        this.sleepWarningMessage = sleepWarningMessage;
        this.multipliers = multipliers;
        this.mutators = mutators;
    }

    public static class Multipliers {
        private static final Function<List<Double>, DataResult<List<Double>>> LUNAR_CYCLE = l -> {
            if (l.isEmpty())
                return DataResult.success(Collections.emptyList());
            if (l.size() != 8)
                return DataResult.error("Lunar cycles multiplier list must have exactly 8 entries!");
            return DataResult.success(l);
        };

        public static final Codec<Multipliers> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.DOUBLE.listOf().flatXmap(LUNAR_CYCLE, LUNAR_CYCLE).fieldOf("lunarMultipliers").forGetter(m -> m.lunarMultipliers),
                        Codec.mapPair( //nested pairs to act as triple
                                SerializationUtils.positiveDouble().fieldOf("scale"),
                                Codec.mapPair(
                                        ResourceLocation.CODEC.listOf().optionalFieldOf("biomes", Collections.emptyList()),
                                        ResourceLocation.CODEC.listOf().optionalFieldOf("dimensions", Collections.emptyList())
                                )
                        ).codec().listOf().fieldOf("locationMultipliers").forGetter(m -> m.locationMultipliers)
                ).apply(inst, Multipliers::new)
        );


        private final List<Double> lunarMultipliers;
        private final List<Pair<Double, Pair<List<ResourceLocation>, List<ResourceLocation>>>> locationMultipliers;
        private final List<ResourceLocation> biomes;
        private final List<ResourceLocation> dimensions;
        private final Map<Pair<Level, Biome>, Double> scaleMap = new HashMap<>();

        public Multipliers(List<Double> lunarMultipliers, List<Pair<Double, Pair<List<ResourceLocation>, List<ResourceLocation>>>> locationMultipliers) {
            this.lunarMultipliers = lunarMultipliers;
            this.locationMultipliers = locationMultipliers;
            this.biomes = locationMultipliers.stream().flatMap(l -> l.getSecond().getFirst().stream()).collect(Collectors.toList());
            this.dimensions = locationMultipliers.stream().flatMap(l -> l.getSecond().getSecond().stream()).collect(Collectors.toList());
        }

        public double getLunarMultiplier(int phase) {
            return lunarMultipliers.isEmpty() ? 1 : lunarMultipliers.get(phase);
        }

        public double getScale(Level world, Biome biome) {
            Pair<Level, Biome> p = new Pair<>(world, biome);
            if (scaleMap.containsKey(p))
                return scaleMap.get(p);

            ResourceLocation dim = world.dimension().location();
            if (!dimensions.contains(dim) && !biomes.contains(biome.getRegistryName())) {
                scaleMap.put(p, 1D);
                return 1D;
            }
            double scale = biomeAndDimMatch(world, biome) * biomeMatch(biome) * dimMatch(world);
            scaleMap.put(p, scale);
            return scale;
        }

        //Check the scales that have specified both a dimension and a biome.
        private double biomeAndDimMatch(Level w, Biome b) {
            ResourceLocation biome = b.getRegistryName();
            ResourceLocation dim = w.dimension().location();
            return locationMultipliers.stream()
                    .filter(p -> p.getSecond().getFirst().contains(biome))
                    .filter(p -> p.getSecond().getSecond().contains(dim))
                    .map(Pair::getFirst)
                    .reduce((d1, d2) -> d1 * d2)
                    .orElse(1D);
        }

        //Check the scales that only have a biome specified
        private double biomeMatch(Biome b) {
            ResourceLocation biome = b.getRegistryName();
            return locationMultipliers.stream()
                    .filter(p -> p.getSecond().getFirst().contains(biome))
                    .filter(p -> p.getSecond().getSecond().isEmpty())
                    .map(Pair::getFirst)
                    .reduce((d1, d2) -> d1 * d2)
                    .orElse(1D);
        }

        //Check the scales that only have a dimension specified
        private double dimMatch(Level w) {
            ResourceLocation dim = w.dimension().location();
            return locationMultipliers.stream()
                    .filter(p -> p.getSecond().getSecond().contains(dim))
                    .filter(p -> p.getSecond().getFirst().isEmpty())
                    .map(Pair::getFirst)
                    .reduce((d1, d2) -> d1 * d2)
                    .orElse(1D);
        }
    }

    public record Mutators(Supplier<Expression> onBlightKilled,
                           Supplier<Expression> onHostileKilled,
                           Supplier<Expression> onPeacefulKilled,
                           Supplier<Expression> onPlayerKilled,
                           Supplier<Expression> onPlayerDeath,
                           Supplier<Expression> onPlayerSleep,
                           List<Pair<List<ResourceLocation>, Supplier<Expression>>> byEntity) {
        private static final Function<ResourceLocation, DataResult<ResourceLocation>> ONLY_ENTITES = rl ->
                ForgeRegistries.ENTITIES.containsKey(rl) ? DataResult.success(rl) : DataResult.error(rl + " is not an entity!");

        public static final Codec<Mutators> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        SerializationUtils.EXPRESSION_CODEC.fieldOf("onBlightKilled").forGetter(m -> m.onBlightKilled),
                        SerializationUtils.EXPRESSION_CODEC.fieldOf("onHostileKilled").forGetter(m -> m.onHostileKilled),
                        SerializationUtils.EXPRESSION_CODEC.fieldOf("onPeacefulKilled").forGetter(m -> m.onPeacefulKilled),
                        SerializationUtils.EXPRESSION_CODEC.fieldOf("onPlayerKilled").forGetter(m -> m.onPlayerKilled),
                        SerializationUtils.EXPRESSION_CODEC.fieldOf("onPlayerDeath").forGetter(m -> m.onPlayerDeath),
                        SerializationUtils.EXPRESSION_CODEC.fieldOf("onPlayerSleep").forGetter(m -> m.onPlayerSleep),
                        Codec.mapPair(
                                ResourceLocation.CODEC.flatXmap(ONLY_ENTITES, ONLY_ENTITES).listOf().fieldOf("entities"),
                                SerializationUtils.EXPRESSION_CODEC.fieldOf("onKilled")
                        ).codec().listOf().fieldOf("byEntity").forGetter(m -> m.byEntity)
                ).apply(inst, Mutators::new)
        );

    }
}
