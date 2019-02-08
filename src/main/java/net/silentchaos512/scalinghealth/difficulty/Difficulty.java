package net.silentchaos512.scalinghealth.difficulty;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultyAffected;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultySource;
import net.silentchaos512.scalinghealth.capability.IDifficultyAffected;
import net.silentchaos512.scalinghealth.capability.IDifficultySource;
import net.silentchaos512.scalinghealth.lib.AreaDifficultyMode;

import java.util.ArrayList;
import java.util.Collection;

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
        for (EntityPlayer player : world.getPlayers(EntityPlayer.class, p -> p.getDistanceSq(center) < rsq)) {
            player.getCapability(CapabilityDifficultySource.INSTANCE).ifPresent(source ->
                    list.add(new Tuple<>(player.getPosition(), source)));
        }

        // TODO: Tile entities that provide difficulty?

        return list;
    }

    public static boolean enabledIn(World world) {
        // TODO: Check configs
        // TODO: Game rules not working..
//        return ModGameRules.DIFFICULTY.getBoolean(world);
        return true;
    }

    public static double forPos(World world, BlockPos pos) {
        return areaMode(world).getAreaDifficulty(world, pos);
    }

    public static double withGroupBonus(World world, double difficulty) {
        // TODO
//        return difficulty * EvalVars.apply(config, world, pos, null, config.difficulty.groupAreaBonus.get());
        return difficulty;
    }

    public static AreaDifficultyMode areaMode(World world) {
        // TODO: Config
        return AreaDifficultyMode.WEIGHTED_AVERAGE;
    }

    public static double clamp(World world, double difficulty) {
        return MathHelper.clamp(difficulty, minValue(world), maxValue(world));
    }

    public static int searchRadius(World world) {
        // TODO: Config
        final int radius = 128;
        return radius <= 0 ? Integer.MAX_VALUE : radius;
    }

    public static long searchRadiusSquared(World world) {
        long radius = searchRadius(world);
        return radius * radius;
    }

    public static double distanceFactor(World world) {
        // TODO: Config
        return 0.0025;
    }

    public static double minValue(World world) {
        // TODO: Config
        return 0;
    }

    public static double maxValue(World world) {
        // TODO: Config
        return 250;
    }
}
