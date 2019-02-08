package net.silentchaos512.scalinghealth.utils;

import com.udojava.evalex.Expression;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.IMob;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.IDifficultyAffected;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.DimensionConfig;
import net.silentchaos512.scalinghealth.config.EvalVars;
import net.silentchaos512.scalinghealth.lib.MobHealthMode;

import java.util.Random;

import static net.silentchaos512.scalinghealth.lib.MobHealthMode.MULTI_HALF;

public final class MobDifficultyHandler {
    private MobDifficultyHandler() {}

    public static void process(EntityLivingBase entity, IDifficultyAffected data) {
        // Already dead?
        if (entity.removed) return;

        World world = entity.world;

        float difficulty = data.getDifficulty();
        float originalMaxHealth = entity.getMaxHealth();

        Random rand = ScalingHealth.random;
        boolean isHostile = entity instanceof IMob;

        // Lunar phase multipliers?
//        if (Config.Difficulty.DIFFICULTY_LUNAR_MULTIPLIERS_ENABLED && world.getWorldTime() % 24000 > 12000) {
//            int moonPhase = world.provider.getMoonPhase(world.getWorldTime()) % 8;
//            float multi = Config.Difficulty.DIFFICULTY_LUNAR_MULTIPLIERS[moonPhase];
//            difficulty *= multi;
//        }

        // Make blight?
        if (Difficulty.canBecomeBlight(entity)) {
            double chance = getBlightChance(entity);
            if (rand.nextFloat() < chance) {
                difficulty *= getBlightDifficultyMultiplier(world);
                data.setIsBlight(true);
            }
        }

        double healthBoost = difficulty;
        double damageBoost = 0;

        IAttributeInstance attributeMaxHealth = entity.getAttribute(SharedMonsterAttributes.MAX_HEALTH);
        double baseMaxHealth = attributeMaxHealth.getBaseValue();
        double healthMultiplier = isHostile
                ? 0.5 //Config.Mob.Health.hostileHealthMultiplier
                : 0.25; //Config.Mob.Health.peacefulHealthMultiplier;

        healthBoost *= healthMultiplier;

        if (difficulty > 0) {
            double diffIncrease = 2 * healthMultiplier * difficulty * rand.nextFloat();
            healthBoost += diffIncrease;
        }

        // Increase attack damage.
        if (difficulty > 0) {
            float diffIncrease = difficulty * rand.nextFloat();
            damageBoost = diffIncrease * 0.1; //Config.Mob.damageMultiplier;
            // Clamp the value so it doesn't go over the maximum config.
//            if (Config.Mob.maxDamageBoost > 0f)
//                genAddedDamage = MathHelper.clamp(genAddedDamage, 0f, Config.Mob.maxDamageBoost);
        }

        // Random potion effect
//        float potionChance = isHostile
//                ? Config.Mob.hostilePotionChance
//                : Config.Mob.passivePotionChance;
//        if (difficulty > 0 && rand.nextFloat() < potionChance) {
//            MobPotionMap.PotionEntry pot = potionMap.getRandom(rand, (int) difficulty);
//            if (pot != null) {
//                entity.addPotionEffect(new PotionEffect(pot.potion, POTION_APPLY_TIME));
//            }
//        }

        // Apply extra health and damage.
        MobHealthMode mode = getHealthMode(entity);
        double healthModAmount = mode.getModifierValue(healthBoost, baseMaxHealth);
        ModifierHandler.addMaxHealth(entity, healthModAmount, mode.getOperator());
        ModifierHandler.addAttackDamage(entity, damageBoost, 0);

        // Heal.
        float healthChange = entity.getMaxHealth() - originalMaxHealth;
        if (Math.abs(healthChange) > 0.01) {
            entity.setHealth(entity.getHealth() + healthChange);
        }
    }

    private static MobHealthMode getHealthMode(EntityLivingBase entity) {
        return MULTI_HALF;
    }

    private static double getBlightChance(EntityLivingBase entity) {
        DimensionConfig config = Config.get(entity);
        // TODO: Pull from dimension config
        Expression expr = new Expression("0.0625 * areaDifficulty / maxDifficulty");
        return EvalVars.apply(config, entity.world, entity.getPosition(), null, expr);
    }

    private static double getBlightDifficultyMultiplier(World world) {
        return 3;
    }
}
