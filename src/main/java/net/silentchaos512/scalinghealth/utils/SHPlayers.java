package net.silentchaos512.scalinghealth.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.DimensionConfig;
import net.silentchaos512.scalinghealth.config.EvalVars;
import net.silentchaos512.scalinghealth.config.RegenConfig;
import net.silentchaos512.utils.MathUtils;

/**
 * Utility class for player-related settings. Same as with {@link SHDifficulty}, this should put a
 * stop to the tangled mess of config references.
 */
public final class SHPlayers {
    private SHPlayers() {throw new IllegalAccessError("Utility class");}

    public static int startingHealth(PlayerEntity player) {
        return Config.get(player).player.startingHealth.get();
    }

    public static int minHealth(PlayerEntity player) {
        return Config.get(player).player.minHealth.get();
    }

    public static int maxHealth(PlayerEntity player) {
        int value = Config.get(player).player.maxHealth.get();
        return value <= 0 ? Integer.MAX_VALUE : value;
    }

    public static int maxHeartCrystals(PlayerEntity player) {
        return (maxHealth(player) - startingHealth(player)) / 2;
    }

    public static int maxAttackDamage(PlayerEntity player) {
        int value = Config.get(player).player.maxAttackDamage.get();
        return value <= 0 ? Integer.MAX_VALUE : value;
    }

    public static int maxPowerCrystals(PlayerEntity player) {
        return (int) ((maxAttackDamage(player) - 1) / SHItems.powerCrystalIncreaseAmount(player));
    }

    public static int clampExtraHearts(PlayerEntity player, int value) {
        return MathUtils.clamp(value,
                (minHealth(player) - startingHealth(player)) / 2,
                (maxHealth(player) - startingHealth(player)) / 2
        );
    }

    public static int clampPowerCrystals(PlayerEntity player, int value) {
        return MathUtils.clamp(value,
                0,
                maxPowerCrystals(player)
        );
    }

    public static int getCrystalCountFromHealth(PlayerEntity player, float health) {
        return (int) ((health - startingHealth(player)) / 2);
    }

    public static float getHealthAfterDeath(PlayerEntity player, DimensionType deathDimension) {
        DimensionConfig config = Config.get(deathDimension);
        int deathHp = (int) EvalVars.apply(config, player, config.player.setHealthOnDeath.get());
        int maxHp = maxHealth(player);
        int minHp = minHealth(player);
        int hp = deathHp < minHp ? minHp : deathHp;
        hp = hp > maxHp ? maxHp : hp;
        return hp;
    }

    public static RegenConfig getRegenConfig(World world){
        return Config.get(world).player.regen;
    }

    public static boolean xpModeEnabled(World world){
        return xpMode(world) != 0;
    }

    public static boolean hpDisabledByXp(World world){
        return xpMode(world) != 1;
    }

    public static int xpMode(World world){
        return Config.get(world).player.healthByXp.get();
    }

    public static int levelsPerHp(World world){
        return Config.get(world).player.levelsPerHp.get();
    }

    public static int hpPerLevel(World world){
        return Config.get(world).player.hpPerLevel.get();
    }
}
