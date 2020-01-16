package net.silentchaos512.scalinghealth.utils;

import com.udojava.evalex.Expression;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.IDifficultyAffected;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.DimensionConfig;
import net.silentchaos512.scalinghealth.config.EvalVars;
import net.silentchaos512.scalinghealth.lib.MobHealthMode;
import net.silentchaos512.scalinghealth.lib.EntityGroup;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.utils.MathUtils;

import java.util.function.Supplier;

public final class MobDifficultyHandler {
    private MobDifficultyHandler() {}

    public static void process(MobEntity entity, IDifficultyAffected data) {
        // Already dead?
        if (!entity.isAlive()) return;

        // Make blight?
        boolean makeBlight = shouldBecomeBlight(entity, data.affectiveDifficulty(entity.world));
        setEntityProperties(entity, data, makeBlight);
    }

    public static boolean shouldBecomeBlight(MobEntity entity, float difficulty) {
        if (Difficulty.canBecomeBlight(entity)) {
            double chance = getBlightChance(entity, difficulty);
            return MathUtils.tryPercentage(ScalingHealth.random, chance);
        }
        return false;
    }

    public static void setEntityProperties(MobEntity entity, IDifficultyAffected data, boolean makeBlight) {
        if (!entity.isAlive()) return;

        World world = entity.world;
        boolean isHostile = entity instanceof IMob;

        if (makeBlight) {
            data.setIsBlight(true);
            if(EntityGroup.from(entity) == EntityGroup.BOSS){
                StringTextComponent name = new StringTextComponent("Blight " + entity.getName().getString());
                name.setStyle(new Style().setColor(TextFormatting.DARK_PURPLE));
                entity.setCustomName(name);
            }
        }

        //Get difficulty after making blight or not. This will determine if a blight's diff is multiplied
        final float difficulty = data.affectiveDifficulty(world);

        double healthBoost = difficulty;
        double damageBoost = 0;

        IAttributeInstance attributeMaxHealth = entity.getAttribute(SharedMonsterAttributes.MAX_HEALTH);
        double baseMaxHealth = attributeMaxHealth.getBaseValue();
        double healthMultiplier = isHostile
                ? Config.get(world).mobs.hostileMultiplier.get()
                : Config.get(world).mobs.passiveMultiplier.get();

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
        Config.get(entity).mobs.randomPotions.tryApply(entity, difficulty);
        //Difficulty.equipAll(entity);

        // Apply extra health and damage.
        MobHealthMode mode = EntityGroup.from(entity).getHealthMode(entity);
        double healthModAmount = mode.getModifierValue(healthBoost, baseMaxHealth);
        ModifierHandler.addMaxHealth(entity, healthModAmount, mode.getOperator());
        ModifierHandler.addAttackDamage(entity, damageBoost, AttributeModifier.Operation.ADDITION);
    }

    private static double getBlightChance(MobEntity entity, float difficulty) {
        return 0.0625 * difficulty / Difficulty.maxValue(entity.world);
    }
}
