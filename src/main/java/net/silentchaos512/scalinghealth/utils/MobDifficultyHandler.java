package net.silentchaos512.scalinghealth.utils;

import net.minecraft.block.Blocks;
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
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.IDifficultyAffected;
import net.silentchaos512.scalinghealth.event.ScalingHealthCommonEvents;
import net.silentchaos512.scalinghealth.lib.MobHealthMode;
import net.silentchaos512.scalinghealth.lib.EntityGroup;
import net.silentchaos512.utils.MathUtils;

public final class MobDifficultyHandler {
    private MobDifficultyHandler() {}

    public static void process(MobEntity entity, IDifficultyAffected data) {
        // Already dead?
        if (!entity.isAlive()) return;

        // Make blight?
        //techincally here getDiff is used not affectiveDiff since the blight modifier has no play (deciding to make it blight or not)
        boolean makeBlight = shouldBecomeBlight(entity, data.getDifficulty());
        setEntityProperties(entity, data, makeBlight);
    }

    public static boolean shouldBecomeBlight(MobEntity entity, float difficulty) {
        if (!SHMobs.canBecomeBlight(entity))    return false;

        double chance = getBlightChance(entity, difficulty);
        if(chance == 1)    return true;

        return MathUtils.tryPercentage(ScalingHealth.random, chance);
    }

    private static double getBlightChance(MobEntity entity, float difficulty) {
        return SHMobs.blightChance(entity) * difficulty / SHDifficulty.maxValue(entity.world);
    }

    public static void setEntityProperties(MobEntity entity, IDifficultyAffected data, boolean makeBlight) {
        if (!entity.isAlive()) return;

        World world = entity.world;
        boolean isHostile = entity instanceof IMob;

        if (makeBlight) {
            data.setIsBlight(true);
            if(EntityGroup.from(entity) == EntityGroup.BOSS){
                StringTextComponent name = (StringTextComponent) new StringTextComponent("Blight " + entity.getName().getString()).setStyle(new Style().setColor(TextFormatting.DARK_PURPLE));
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
                ? SHMobs.healthHostileMultiplier(world)
                : SHMobs.healthPassiveMultiplier(world);

        healthBoost *= healthMultiplier;

        if (difficulty > 0) {
            double diffIncrease = 2 * healthMultiplier * difficulty * ScalingHealth.random.nextFloat();
            healthBoost += diffIncrease;
        }

        if(ScalingHealthCommonEvents.spawnerSpawns.contains(entity.getUniqueID())){
            ScalingHealth.LOGGER.debug("Previous boost: {}", healthBoost);
            healthBoost *= SHMobs.spawnerHealth(world);
            ScalingHealthCommonEvents.spawnerSpawns.remove(entity.getUniqueID());
            ScalingHealth.LOGGER.debug("Mob spawned in a spawner with {} hp, boost {}", entity.getMaxHealth()+healthBoost, healthBoost);
        }

        // Increase attack damage.
        if (difficulty > 0) {
            float diffIncrease = difficulty * ScalingHealth.random.nextFloat();
            damageBoost = diffIncrease * SHMobs.damageBoostScale(entity);
            // Clamp the value so it doesn't go over the maximum config.
            double max = SHMobs.maxDamageBoost(entity);
            if (max > 0f) {
                damageBoost = MathHelper.clamp(damageBoost, 0, max);
            }
        }

        // Random potion effect
        SHMobs.getMobPotionConfig(world).tryApply(entity, difficulty);
        SHMobEquipment.equipAll(entity);

        // Apply extra health and damage.
        MobHealthMode mode = EntityGroup.from(entity).getHealthMode(entity);
        double healthModAmount = mode.getModifierValue(healthBoost, baseMaxHealth);
        ModifierHandler.addMaxHealth(entity, healthModAmount, mode.getOperator());
        ModifierHandler.addAttackDamage(entity, damageBoost, AttributeModifier.Operation.ADDITION);
    }
}
