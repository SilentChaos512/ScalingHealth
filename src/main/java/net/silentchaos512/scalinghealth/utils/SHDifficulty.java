package net.silentchaos512.scalinghealth.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.silentchaos512.lib.util.MCMathUtils;
import net.silentchaos512.scalinghealth.capability.DifficultyAffectedCapability;
import net.silentchaos512.scalinghealth.capability.DifficultySourceCapability;
import net.silentchaos512.scalinghealth.capability.IDifficultyAffected;
import net.silentchaos512.scalinghealth.capability.IDifficultySource;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.EvalVars;
import net.silentchaos512.scalinghealth.config.GameConfig;
import net.silentchaos512.scalinghealth.lib.AreaDifficultyMode;
import net.silentchaos512.utils.MathUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public final class SHDifficulty {
    private SHDifficulty() {throw new IllegalAccessError("Utility class");}

    public static IDifficultyAffected affected(ICapabilityProvider entity) {
        return entity.getCapability(DifficultyAffectedCapability.INSTANCE)
                .orElseGet(DifficultyAffectedCapability::new);
    }

    public static IDifficultySource source(ICapabilityProvider source) {
        return source.getCapability(DifficultySourceCapability.INSTANCE)
                .orElseGet(DifficultySourceCapability::new);
    }

    public static void setSourceDifficulty(PlayerEntity player, double difficulty){
        IDifficultySource source = SHDifficulty.source(player);
        if (!MathUtils.doublesEqual(source.getDifficulty(), difficulty)) {
            // Update difficulty after sleeping
            source.setDifficulty((float) difficulty);                               //player diff
            SHDifficulty.source(player.world).setDifficulty((float) difficulty);    //world diff
        }
    }

    @SuppressWarnings("TypeMayBeWeakened")
    public static double getDifficultyOf(Entity entity) {
        if (entity instanceof PlayerEntity)
            return source(entity).getDifficulty();
        return affected(entity).affectiveDifficulty();
    }

    public static Collection<Tuple<BlockPos, IDifficultySource>> allPlayerSources(IWorld world, Vector3i center, long radius) {
        Collection<Tuple<BlockPos, IDifficultySource>> list = new ArrayList<>();

        // Get players
        playersInRange(world, center, radius).forEach(player -> list.add(new Tuple<>(player.getPosition(), SHDifficulty.source(player))));
        return list;
    }

    public static Stream<? extends PlayerEntity> playersInRange(IWorld world, Vector3i center, long radius) {
        return world.getPlayers().stream().filter(p -> radius <= 0 || MCMathUtils.distanceSq(p, center) < searchRadiusSquared());
    }

    public static int searchRadius() {
        final int radius = Config.GENERAL.difficulty.searchRadius.get();
        return radius <= 0 ? Integer.MAX_VALUE : radius;
    }

    public static long searchRadiusSquared() {
        final long radius = searchRadius();
        return radius * radius;
    }

    public static double areaDifficulty(World world, BlockPos pos) {
        return areaDifficulty(world, pos, true);
    }

    public static double areaDifficulty(World world, BlockPos pos, boolean groupBonus) {
        return areaMode().getAreaDifficulty(world, pos, groupBonus);
    }

    public static double locationMultiplier(World world, BlockPos pos) {
        return Config.GENERAL.difficulty.getLocationMultiplier(world, pos);
    }

    public static double lunarMultiplier(World world) {
        GameConfig config = Config.GENERAL;
        if (!config.difficulty.lunarCyclesEnabled.get()) return 1.0;
        List<? extends Double> values = config.difficulty.lunarCycleMultipliers.get();
        if (values.isEmpty()) return 1.0;
        int phase = world.getDimensionType().getMoonPhase(world.func_241851_ab());
        return values.get(MathHelper.clamp(phase, 0, values.size() - 1));
    }

    public static double withGroupBonus(World world, BlockPos pos, double difficulty) {
        return difficulty * EvalVars.apply(world, pos, null, Config.GENERAL.difficulty.groupAreaBonus.get());
    }

    public static AreaDifficultyMode areaMode() {
        return Config.GENERAL.difficulty.areaMode.get();
    }

    public static double clamp(double difficulty) {
        return MathHelper.clamp(difficulty, minValue(), maxValue());
    }

    public static boolean ignoreYAxis(){
        return Config.GENERAL.difficulty.ignoreYAxis.get();
    }

    public static double distanceFactor() {
        return Config.GENERAL.difficulty.distanceFactor.get();
    }

    public static double minValue() {
        return Config.GENERAL.difficulty.minValue.get();
    }

    public static double maxValue() {
        return Config.GENERAL.difficulty.maxValue.get();
    }

    public static double changePerSecond() {
        return Config.GENERAL.difficulty.changePerSecond.get();
    }

    public static double idleModifier() {
        return Config.GENERAL.difficulty.idleMultiplier.get();
    }

    public static boolean afkMessage(){
        return Config.GENERAL.difficulty.afkMessage.get();
    }

    public static double timeBeforeAfk() {
        return Config.GENERAL.difficulty.timeBeforeAfk.get();
    }

    public static boolean isPlayerExempt(PlayerEntity player){
        return Config.GENERAL.difficulty.isPlayerExempt(player);
    }

    public static double getDifficultyAfterDeath(PlayerEntity player) {
        return EvalVars.apply(player, Config.GENERAL.difficulty.onPlayerDeath.get());
    }

    public static double applyKillMutator(MobEntity entity, PlayerEntity player){
        return EvalVars.apply(player, Config.GENERAL.difficulty.getKillMutator(entity));
    }

    public static double diffOnPlayerSleep(PlayerEntity entity){
        return EvalVars.apply(entity, Config.GENERAL.difficulty.onPlayerSleep.get());
    }

    public static String sleepWarningMessage(){
        return Config.GENERAL.difficulty.sleepWarningMessage.get();
    }

    public static List<? extends String> getDamageBlacklistedMods(){
        return Config.GENERAL.damageScaling.modBlacklist.get();
    }
}
