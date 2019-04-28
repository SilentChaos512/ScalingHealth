package net.silentchaos512.scalinghealth.utils;

import com.udojava.evalex.Expression;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.IDifficultyAffected;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.DimensionConfig;
import net.silentchaos512.scalinghealth.config.EvalVars;
import net.silentchaos512.scalinghealth.lib.MobHealthMode;
import net.silentchaos512.scalinghealth.lib.MobType;
import net.silentchaos512.utils.MathUtils;

public final class MobDifficultyHandler {
    private MobDifficultyHandler() {}

    public static void process(EntityLivingBase entity, IDifficultyAffected data) {
        // Already dead?
        if (!entity.isAlive()) return;

        float difficulty = data.getDifficulty();

        // Make blight?
        boolean makeBlight = shouldBecomeBlight(entity, difficulty);
        setEntityProperties(entity, data, difficulty, makeBlight);
    }

    public static boolean shouldBecomeBlight(EntityLivingBase entity, float difficulty) {
        if (Difficulty.canBecomeBlight(entity)) {
            double chance = getBlightChance(entity);
            return MathUtils.tryPercentage(ScalingHealth.random, chance);
        }
        return false;
    }

    public static void setEntityProperties(EntityLivingBase entity, IDifficultyAffected data, float difficulty, boolean makeBlight) {
        if (!entity.isAlive()) return;

        World world = entity.world;
        boolean isHostile = entity instanceof IMob;

        if (makeBlight) {
            difficulty *= getBlightDifficultyMultiplier(world);
            data.setIsBlight(true);
        }

        final float totalDifficulty = difficulty;

        double healthBoost = difficulty;
        double damageBoost = 0;

        IAttributeInstance attributeMaxHealth = entity.getAttribute(SharedMonsterAttributes.MAX_HEALTH);
        double baseMaxHealth = attributeMaxHealth.getBaseValue();
        double healthMultiplier = isHostile
                ? 0.5 //Config.Mob.Health.hostileHealthMultiplier
                : 0.25; //Config.Mob.Health.peacefulHealthMultiplier;

        healthBoost *= healthMultiplier;

        if (difficulty > 0) {
            double diffIncrease = 2 * healthMultiplier * difficulty * ScalingHealth.random.nextFloat();
            healthBoost += diffIncrease;
        }

        // Increase attack damage.
        if (difficulty > 0) {
            float diffIncrease = difficulty * ScalingHealth.random.nextFloat();
            damageBoost = diffIncrease * Difficulty.damageBoostScale(entity);
            // Clamp the value so it doesn't go over the maximum config.
            double max = Difficulty.maxDamageBoost(entity);
            if (max > 0f) {
                damageBoost = MathHelper.clamp(damageBoost, 0, max);
            }
        }

        // Random potion effect
        Config.get(entity).mobs.randomPotions.tryApply(entity, totalDifficulty);

        // Apply extra health and damage.
        MobHealthMode mode = MobType.from(entity).getHealthMode(entity);
        double healthModAmount = mode.getModifierValue(healthBoost, baseMaxHealth);
        ModifierHandler.addMaxHealth(entity, healthModAmount, mode.getOperator());
        ModifierHandler.addAttackDamage(entity, damageBoost, 0);
    }

    private static double getBlightChance(EntityLivingBase entity) {
        // FIXME: May not line up with actual entity difficulty, need to pass in difficulty
        DimensionConfig config = Config.get(entity);
        Expression expr = new Expression("0.0625 * areaDifficulty / maxDifficulty");
        return EvalVars.apply(config, entity.world, entity.getPosition(), null, expr);
    }

    private static double getBlightDifficultyMultiplier(World world) {
        return 3;
    }
}
