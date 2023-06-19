package net.silentchaos512.scalinghealth.utils.config;

import com.mojang.datafixers.util.Pair;
import com.udojava.evalex.Expression;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.lib.util.MCMathUtils;
import net.silentchaos512.scalinghealth.capability.DifficultyAffectedCapability;
import net.silentchaos512.scalinghealth.capability.DifficultySourceCapability;
import net.silentchaos512.scalinghealth.capability.IDifficultyAffected;
import net.silentchaos512.scalinghealth.capability.IDifficultySource;
import net.silentchaos512.scalinghealth.config.EvalVars;
import net.silentchaos512.scalinghealth.resources.mechanics.DifficultyMechanics;
import net.silentchaos512.scalinghealth.resources.mechanics.SHMechanics;
import net.silentchaos512.scalinghealth.utils.EntityGroup;
import net.silentchaos512.scalinghealth.utils.mode.AreaDifficultyMode;
import net.silentchaos512.utils.MathUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SHDifficulty {
    private SHDifficulty() {throw new IllegalAccessError("Utility class");}
    
    private static DifficultyMechanics getMechanics() {
        return SHMechanics.getMechanics().difficultyMechanics(); // cache?
    }

    public static IDifficultyAffected affected(ICapabilityProvider entity) {
        return entity.getCapability(DifficultyAffectedCapability.INSTANCE)
                .orElseGet(DifficultyAffectedCapability::new);
    }

    public static IDifficultySource source(ICapabilityProvider source) {
        return source.getCapability(DifficultySourceCapability.INSTANCE)
                .orElseGet(DifficultySourceCapability::new);
    }

    public static void setSourceDifficulty(Player player, double difficulty){
        IDifficultySource source = SHDifficulty.source(player);
        if (!MathUtils.doublesEqual(source.getDifficulty(), difficulty)) {
            source.setDifficulty((float) difficulty);                               //player diff
            SHDifficulty.source(player.level()).setDifficulty((float) difficulty);    //world diff
        }
    }

    @SuppressWarnings("TypeMayBeWeakened")
    public static double getDifficultyOf(Entity entity) {
        if (entity instanceof Player)
            return source(entity).getDifficulty();
        return affected(entity).affectiveDifficulty();
    }

    public static List<Pair<IDifficultySource, BlockPos>> positionedPlayerSources(LevelAccessor world, Vec3i center, long radius) {
        return playersInRange(world, center, radius)
                .map(player -> Pair.of(source(player), player.blockPosition()))
                .collect(Collectors.toList());
    }

    public static Collection<Tuple<BlockPos, IDifficultySource>> allPlayerSources(LevelAccessor world, Vec3i center, long radius) {
        Collection<Tuple<BlockPos, IDifficultySource>> list = new ArrayList<>();

        // Get players
        playersInRange(world, center, radius).forEach(player -> list.add(new Tuple<>(player.blockPosition(), SHDifficulty.source(player))));
        return list;
    }

    public static Stream<? extends Player> playersInRange(LevelAccessor world, Vec3i center, long radius) {
        if (radius <= 0)
            return world.players().stream();
        return world.players().stream()
                .filter(p -> MCMathUtils.distanceSq(p, center) < (radius * radius));
    }

    public static int groupSearchRadius() {
        return SHMechanics.getMechanics().difficultyMechanics().groupBonusRadius;
    }

    public static double areaDifficulty(Level world, BlockPos pos) {
        return areaDifficulty(world, pos, true);
    }

    public static double areaDifficulty(Level world, BlockPos pos, boolean groupBonus) {
        return clamp(areaMode().getDifficulty(world, pos) *
                locationMultiplier(world, pos) *
                lunarMultiplier(world) *
                (groupBonus ? groupMultiplier(world, pos) : 1));
    }

    public static double locationMultiplier(Level world, BlockPos pos) {
        Holder<Biome> biome = world.getBiome(pos);
        if (!biome.isBound())
            return 1;
        return getMechanics().multipliers.getScale(world, world.getBiome(pos).value());
    }

    //TODO Can't be checked on the ClientWorld, have to send packet (for debug overlay)
    public static double lunarMultiplier(Level world) {
        return (world.dimension() != Level.OVERWORLD || world.isDay()) ? 1 :
                getMechanics().multipliers
                        .getLunarMultiplier(world.dimensionType().moonPhase(world.dayTime()));
    }

    public static double groupMultiplier(Level world, BlockPos pos) {
        return EvalVars.apply(world, pos, null, getMechanics().groupBonus.get());
    }

    public static AreaDifficultyMode areaMode() {
        return getMechanics().mode;
    }

    public static double clamp(double difficulty) {
        return Mth.clamp(difficulty, minValue(), maxValue());
    }

    public static double minValue() {
        return getMechanics().minValue;
    }

    public static double maxValue() {
        return getMechanics().maxValue;
    }

    public static double changePerSecond() {
        return getMechanics().changePerSecond;
    }

    public static double idleModifier() {
        return getMechanics().idleMultiplier;
    }

    public static boolean afkMessage(){
        return getMechanics().afkMessage;
    }

    public static double timeBeforeAfk() {
        return getMechanics().timeBeforeAfk;
    }

    public static double getDifficultyAfterDeath(Player player) {
        return EvalVars.apply(player, getMechanics().mutators.onPlayerDeath().get());
    }

    public static void applyKillMutator(LivingEntity killed, Player killer) {
        //check if player, if it is, no other mutator can apply
        if (killed instanceof Player) {
            setSourceDifficulty(killer, EvalVars.apply(killer, getMechanics().mutators.onPlayerKilled().get()));
            return;
        }

        //check if blight, continue even if it to apply the base mutator
        if (affected(killed).isBlight())
            setSourceDifficulty(killer, EvalVars.apply(killer, getMechanics().mutators.onBlightKilled().get()));

        //check for entity specific mutators first
        for (Pair<List<ResourceLocation>, Supplier<Expression>> p : getMechanics().mutators.byEntity()) {
            if (p.getFirst().contains(ForgeRegistries.ENTITY_TYPES.getKey(killed.getType()))) {
                setSourceDifficulty(killer, EvalVars.apply(killer, p.getSecond().get()));
                return;
            }
        }

        //finally fall back to categorising entity between peaceful and hostile
        if (EntityGroup.from(killed, true) == EntityGroup.PEACEFUL) {
            setSourceDifficulty(killer, EvalVars.apply(killer, getMechanics().mutators.onPeacefulKilled().get()));
        } else {
            setSourceDifficulty(killer, EvalVars.apply(killer, getMechanics().mutators.onHostileKilled().get()));
        }
    }

    public static double diffOnPlayerSleep(Player entity){
        return EvalVars.apply(entity, getMechanics().mutators.onPlayerSleep().get());
    }

    public static boolean sleepWarningMessage(){
        return getMechanics().sleepWarningMessage;
    }

    public static List<? extends String> getDamageBlacklistedMods(){
        return SHMechanics.getMechanics().damageScalingMechanics().modBlackList;
    }
}
