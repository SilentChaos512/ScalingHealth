package net.silentchaos512.scalinghealth.utils;

import com.udojava.evalex.Expression;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IEntityReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.silentchaos512.lib.util.MCMathUtils;
import net.silentchaos512.scalinghealth.capability.DifficultyAffectedCapability;
import net.silentchaos512.scalinghealth.capability.DifficultySourceCapability;
import net.silentchaos512.scalinghealth.capability.IDifficultyAffected;
import net.silentchaos512.scalinghealth.capability.IDifficultySource;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.DimensionConfig;
import net.silentchaos512.scalinghealth.config.EvalVars;
//import net.silentchaos512.scalinghealth.init.ModGameRules;
import net.silentchaos512.scalinghealth.lib.AreaDifficultyMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Master utility class for difficulty-related stuff. Any calls should be done through this class
 * whenever possible, to prevent the tangled mess of config references from earlier versions.
 */
public final class Difficulty {
    private Difficulty() {throw new IllegalAccessError("Utility class");}

    public static IDifficultyAffected affected(ICapabilityProvider entity) {
        return entity.getCapability(DifficultyAffectedCapability.INSTANCE)
                .orElseGet(DifficultyAffectedCapability::new);
    }

    public static IDifficultySource source(ICapabilityProvider source) {
        return source.getCapability(DifficultySourceCapability.INSTANCE)
                .orElseGet(DifficultySourceCapability::new);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    public static double getDifficultyOf(Entity entity) {
        if (entity instanceof PlayerEntity)
            return source(entity).getDifficulty();
        return affected(entity).getDifficulty();
    }

    public static Collection<Tuple<BlockPos, IDifficultySource>> sources(IEntityReader world, Vec3i center, long radius) {
        Collection<Tuple<BlockPos, IDifficultySource>> list = new ArrayList<>();

        // Get players
        playersInRange(world, center, radius).forEach(player -> {
            player.getCapability(DifficultySourceCapability.INSTANCE).ifPresent(source -> {
                list.add(new Tuple<>(player.getPosition(), source));
            });
        });

        // TODO: Tile entities that provide difficulty?

        return list;
    }

    public static Stream<? extends PlayerEntity> playersInRange(IEntityReader world, Vec3i center, long radius) {
        long radiusSquared = radius * radius;
        return world.getPlayers().stream().filter(p -> radius <= 0 || MCMathUtils.distanceSq(p, center) < radiusSquared);
    }

    public static boolean enabledIn(World world) {
        return Config.get(world).difficulty.maxValue.get() > 0 /*&& ModGameRules.DIFFICULTY.getBoolean(world)*/;
    }

    public static double areaDifficulty(World world, BlockPos pos) {
        return areaDifficulty(world, pos, true);
    }

    public static double areaDifficulty(World world, BlockPos pos, boolean groupBonus) {
        return areaMode(world).getAreaDifficulty(world, pos, groupBonus);
    }

    public static double locationMultiplier(IWorldReader world, BlockPos pos) {
        return Config.get(world).difficulty.getLocationMultiplier(world, pos);
    }

    public static double lunarMultiplier(World world) {
        DimensionConfig config = Config.get(world);
        if (!config.difficulty.lunarCyclesEnabled.get()) return 1.0;
        List<? extends Double> values = config.difficulty.lunarCycleMultipliers.get();
        if (values.isEmpty()) return 1.0;
        int phase = world.getDimension().getMoonPhase(world.getGameTime());
        return values.get(MathHelper.clamp(phase, 0, values.size() - 1));
    }

    public static double withGroupBonus(World world, BlockPos pos, double difficulty) {
        DimensionConfig config = Config.get(world);
        Expression expression = config.difficulty.groupAreaBonus.get();
        return difficulty * EvalVars.apply(config, world, pos, null, expression);
    }

    public static AreaDifficultyMode areaMode(IWorldReader world) {
        return Config.get(world).difficulty.areaMode.get();
    }

    public static double clamp(IWorldReader world, double difficulty) {
        return MathHelper.clamp(difficulty, minValue(world), maxValue(world));
    }

    public static int searchRadius(IWorldReader world) {
        final int radius = Config.get(world).difficulty.searchRadius.get();
        return radius <= 0 ? Integer.MAX_VALUE : radius;
    }

    public static long searchRadiusSquared(IWorldReader world) {
        final long radius = searchRadius(world);
        return radius * radius;
    }

    public static double distanceFactor(IWorldReader world) {
        return Config.get(world).difficulty.distanceFactor.get();
    }

    public static double minValue(IWorldReader world) {
        return Config.get(world).difficulty.minValue.get();
    }

    public static double maxValue(IWorldReader world) {
        return Config.get(world).difficulty.maxValue.get();
    }

    public static double changePerSecond(IWorldReader world) {
        return Config.get(world).difficulty.changePerSecond.get();
    }

    public static boolean allowsDifficultyChanges(MobEntity entity) {
        return true;
    }

    public static boolean canBecomeBlight(MobEntity entity) {
        return Config.get(entity.world).mobs.isMobExempt(entity);
    }

    public static boolean isBlight(MobEntity entity) {
        return entity.getCapability(DifficultyAffectedCapability.INSTANCE)
                .orElseGet(DifficultyAffectedCapability::new)
                .isBlight();
    }

    public static boolean notifyOnDeath(IWorldReader world){
        return Config.get(world).mobs.notifyOnBlightDeath.get();
    }

    public static double damageBoostScale(MobEntity entity) {
        return Config.get(entity).mobs.damageBoostScale.get();
    }

    public static double maxDamageBoost(MobEntity entity) {
        return Config.get(entity).mobs.maxDamageBoost.get();
    }

    public static double getDifficultyAfterDeath(PlayerEntity player, DimensionType deathDimension) {
        DimensionConfig config = Config.get(deathDimension);
        return EvalVars.apply(config, player, config.difficulty.onPlayerDeath.get());
    }
}
