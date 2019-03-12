package net.silentchaos512.scalinghealth.utils;

import com.udojava.evalex.Expression;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultyAffected;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultySource;
import net.silentchaos512.scalinghealth.capability.IDifficultyAffected;
import net.silentchaos512.scalinghealth.capability.IDifficultySource;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.DimensionConfig;
import net.silentchaos512.scalinghealth.config.EvalVars;
import net.silentchaos512.scalinghealth.init.ModGameRules;
import net.silentchaos512.scalinghealth.lib.AreaDifficultyMode;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Master utility class for difficulty-related stuff. Any calls should be done through this class
 * whenever possible, to prevent the tangled mess of config references from earlier versions.
 */
public final class Difficulty {
    private Difficulty() {throw new IllegalAccessError("Utility class");}

    public static IDifficultyAffected affected(ICapabilityProvider entity) {
        return entity.getCapability(CapabilityDifficultyAffected.INSTANCE)
                .orElseGet(CapabilityDifficultyAffected::new);
    }

    public static IDifficultySource source(ICapabilityProvider source) {
        return source.getCapability(CapabilityDifficultySource.INSTANCE)
                .orElseGet(CapabilityDifficultySource::new);
    }

    public static Collection<Tuple<BlockPos, IDifficultySource>> sources(World world, BlockPos center, long radius) {
        long rsq = radius * radius;
        Collection<Tuple<BlockPos, IDifficultySource>> list = new ArrayList<>();

        // Get players
        for (EntityPlayer player : world.getPlayers(EntityPlayer.class, p -> rsq == 0 || p.getDistanceSq(center) < rsq)) {
            player.getCapability(CapabilityDifficultySource.INSTANCE).ifPresent(source ->
                    list.add(new Tuple<>(player.getPosition(), source)));
        }

        // TODO: Tile entities that provide difficulty?

        return list;
    }

    public static boolean enabledIn(World world) {
        return ModGameRules.DIFFICULTY.getBoolean(world)
                && Config.get(world).difficulty.maxValue.get() > 0;
    }

    public static double areaDifficulty(World world, BlockPos pos) {
        return areaDifficulty(world, pos, true);
    }

    public static double areaDifficulty(World world, BlockPos pos, boolean groupBonus) {
        return areaMode(world).getAreaDifficulty(world, pos, groupBonus);
    }

    public static double withGroupBonus(World world, BlockPos pos, double difficulty) {
        DimensionConfig config = Config.get(world);
        Expression expression = config.difficulty.groupAreaBonus.get();
        return difficulty * EvalVars.apply(config, world, pos, null, expression);
    }

    public static AreaDifficultyMode areaMode(IWorldReaderBase world) {
        return Config.get(world).difficulty.areaMode.get();
    }

    public static double clamp(IWorldReaderBase world, double difficulty) {
        return MathHelper.clamp(difficulty, minValue(world), maxValue(world));
    }

    public static int searchRadius(IWorldReaderBase world) {
        final int radius = Config.get(world).difficulty.searchRadius.get();
        return radius <= 0 ? Integer.MAX_VALUE : radius;
    }

    public static long searchRadiusSquared(IWorldReaderBase world) {
        final long radius = searchRadius(world);
        return radius * radius;
    }

    public static double distanceFactor(IWorldReaderBase world) {
        return Config.get(world).difficulty.distanceFactor.get();
    }

    public static double minValue(IWorldReaderBase world) {
        return Config.get(world).difficulty.minValue.get();
    }

    public static double maxValue(IWorldReaderBase world) {
        return Config.get(world).difficulty.maxValue.get();
    }

    public static double changePerSecond(IWorldReaderBase world) {
        return Config.get(world).difficulty.changePerSecond.get();
    }

    public static boolean allowsDifficultyChanges(EntityLivingBase entity) {
        return true;
    }

    public static boolean canBecomeBlight(EntityLivingBase entity) {
        return true;
    }

    public static boolean isBlight(EntityLivingBase entity) {
        IDifficultyAffected affected = entity.getCapability(CapabilityDifficultyAffected.INSTANCE)
                .orElseGet(CapabilityDifficultyAffected::new);
        return affected.isBlight();
    }

    public static double damageBoostScale(EntityLivingBase entity) {
        return Config.get(entity).mobs.damageBoostScale.get();
    }

    public static double maxDamageBoost(EntityLivingBase entity) {
        return Config.get(entity).mobs.maxDamageBoost.get();
    }

    public static double getDifficultyAfterDeath(EntityPlayer player, DimensionType deathDimension) {
        DimensionConfig config = Config.get(deathDimension);
        return EvalVars.apply(config, player.world, player.getPosition(), player, config.difficulty.onPlayerDeath.get());
    }
}
