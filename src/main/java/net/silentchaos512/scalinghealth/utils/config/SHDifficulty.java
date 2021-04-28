package net.silentchaos512.scalinghealth.utils.config;

import com.mojang.datafixers.util.Pair;
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
import net.silentchaos512.scalinghealth.config.EvalVars;
import net.silentchaos512.scalinghealth.resources.mechanics.SHMechanicListener;
import net.silentchaos512.scalinghealth.utils.mode.AreaDifficultyMode;
import net.silentchaos512.utils.MathUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
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

    public static List<Pair<IDifficultySource, BlockPos>> positionedPlayerSources(IWorld world, Vector3i center, long radius) {
        return playersInRange(world, center, radius)
                .map(player -> Pair.of(source(player), player.getPosition()))
                .collect(Collectors.toList());
    }

    public static Collection<Tuple<BlockPos, IDifficultySource>> allPlayerSources(IWorld world, Vector3i center, long radius) {
        Collection<Tuple<BlockPos, IDifficultySource>> list = new ArrayList<>();

        // Get players
        playersInRange(world, center, radius).forEach(player -> list.add(new Tuple<>(player.getPosition(), SHDifficulty.source(player))));
        return list;
    }

    public static Stream<? extends PlayerEntity> playersInRange(IWorld world, Vector3i center, long radius) {
        if (radius <= 0)
            return world.getPlayers().stream();
        return world.getPlayers().stream()
                .filter(p -> MCMathUtils.distanceSq(p, center) < (radius * radius));
    }

    public static int groupSearchRadius() {
        return SHMechanicListener.getDifficultyMechanics().groupBonusRadius;
    }

    public static double areaDifficulty(World world, BlockPos pos) {
        return areaDifficulty(world, pos, true);
    }

    public static double areaDifficulty(World world, BlockPos pos, boolean groupBonus) {
        return clamp(areaMode().getDifficulty(world, pos) *
                locationMultiplier(world, pos) *
                lunarMultiplier(world) *
                (groupBonus ? groupMultiplier(world, pos) : 1));
    }

    public static double locationMultiplier(World world, BlockPos pos) {
        return SHMechanicListener.getDifficultyMechanics().multipliers.getScale(world, world.getBiome(pos));
    }

    public static double lunarMultiplier(World world) {
        return world.isDaytime() ? 1 : SHMechanicListener.getDifficultyMechanics().multipliers
                .getLunarMultiplier(world.getDimensionType().getMoonPhase(world.func_241851_ab()));
    }

    public static double groupMultiplier(World world, BlockPos pos) {
        return EvalVars.apply(world, pos, null, SHMechanicListener.getDifficultyMechanics().groupBonus.get());
    }

    public static AreaDifficultyMode areaMode() {
        return SHMechanicListener.getDifficultyMechanics().mode;
    }

    public static double clamp(double difficulty) {
        return MathHelper.clamp(difficulty, minValue(), maxValue());
    }

    public static double minValue() {
        return SHMechanicListener.getDifficultyMechanics().minValue;
    }

    public static double maxValue() {
        return SHMechanicListener.getDifficultyMechanics().maxValue;
    }

    public static double changePerSecond() {
        return SHMechanicListener.getDifficultyMechanics().changePerSecond;
    }

    public static double idleModifier() {
        return SHMechanicListener.getDifficultyMechanics().idleMultiplier;
    }

    public static boolean afkMessage(){
        return SHMechanicListener.getDifficultyMechanics().afkMessage;
    }

    public static double timeBeforeAfk() {
        return SHMechanicListener.getDifficultyMechanics().timeBeforeAfk;
    }

    public static double getDifficultyAfterDeath(PlayerEntity player) {
        return EvalVars.apply(player, SHMechanicListener.getDifficultyMechanics().mutators.onPlayerDeath.get());
    }

    public static double applyKillMutator(MobEntity entity, PlayerEntity player){
        return EvalVars.apply(player, SHMechanicListener.getDifficultyMechanics().mutators.onPlayerKilled.get());
    }

    public static double diffOnPlayerSleep(PlayerEntity entity){
        return EvalVars.apply(entity, SHMechanicListener.getDifficultyMechanics().mutators.onPlayerSleep.get());
    }

    public static boolean sleepWarningMessage(){
        return SHMechanicListener.getDifficultyMechanics().sleepWarningMessage;
    }

    public static List<? extends String> getDamageBlacklistedMods(){
        return SHMechanicListener.getDamageScalingMechanics().modBlackList;
    }
}
