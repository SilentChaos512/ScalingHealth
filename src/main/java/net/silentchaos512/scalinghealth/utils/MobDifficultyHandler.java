package net.silentchaos512.scalinghealth.utils;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.IDifficultyAffected;
import net.silentchaos512.scalinghealth.event.BlightHandler;
import net.silentchaos512.scalinghealth.event.ScalingHealthCommonEvents;
import net.silentchaos512.scalinghealth.lib.EntityGroup;
import net.silentchaos512.scalinghealth.lib.MobHealthMode;
import net.silentchaos512.scalinghealth.network.ClientBlightMessage;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.utils.MathUtils;

public final class MobDifficultyHandler {
    private MobDifficultyHandler() {}

    public static void process(MobEntity entity, IDifficultyAffected data) {
        if (!entity.isAlive()) return;
        setEntityProperties(entity, data, shouldBecomeBlight(entity, data.getDifficulty()));
    }

    public static boolean shouldBecomeBlight(MobEntity entity, float difficulty) {
        if (!SHMobs.canBecomeBlight(entity))    return false;

        double chance = getBlightChance(difficulty);
        if(chance == 1)    return true;

        return MathUtils.tryPercentage(ScalingHealth.random, chance);
    }

    private static double getBlightChance(float difficulty) {
        return SHMobs.blightChance() * difficulty / SHDifficulty.maxValue();
    }

    public static void setEntityProperties(MobEntity entity, IDifficultyAffected data, boolean makeBlight) {
        if (!entity.isAlive()) return;

        boolean isHostile = entity instanceof IMob;

        if (makeBlight) {
            data.setIsBlight(true);
            ClientBlightMessage msg = new ClientBlightMessage(entity.getEntityId());
            Network.channel.send(PacketDistributor.TRACKING_ENTITY.with(()->entity), msg);

            BlightHandler.applyBlightPotionEffects(entity);
            if(EntityGroup.from(entity) == EntityGroup.BOSS){
                ITextComponent blight = new TranslationTextComponent("misc.scalinghealth.blight", entity.getDisplayName()).applyTextStyle(TextFormatting.DARK_PURPLE);
                entity.setCustomName(blight);
            }
        }

        //Get difficulty after making blight or not. This will determine if a blight's diff is multiplied
        final float difficulty = data.affectiveDifficulty(); if(difficulty <= 0) return;

        // Random potion effect
        SHMobs.getMobPotionConfig().tryApply(entity, difficulty);

        if(EnabledFeatures.mobHpIncreaseEnabled()){

            double healthBoost = difficulty;
            IAttributeInstance attributeMaxHealth = entity.getAttribute(SharedMonsterAttributes.MAX_HEALTH);
            double baseMaxHealth = attributeMaxHealth.getBaseValue();
            double healthMultiplier = isHostile
                    ? SHMobs.healthHostileMultiplier()
                    : SHMobs.healthPassiveMultiplier();

            healthBoost *= healthMultiplier;

            double diffIncrease = 2 * healthMultiplier * difficulty * ScalingHealth.random.nextFloat();
            healthBoost += diffIncrease;

            if(ScalingHealthCommonEvents.spawnerSpawns.contains(entity.getUniqueID())){
                healthBoost *= SHMobs.spawnerHealth();
                ScalingHealthCommonEvents.spawnerSpawns.remove(entity.getUniqueID());
            }

            // Apply extra health and damage.
            MobHealthMode mode = EntityGroup.from(entity).getHealthMode();
            double healthModAmount = mode.getModifierValue(healthBoost, baseMaxHealth);
            ModifierHandler.setMaxHealth(entity, healthModAmount, mode.getOperator());
        }

        // Increase attack damage.
        if(EnabledFeatures.mobDamageIncreaseEnabled()){
            double damageBoost;

            float diffIncrease = difficulty * ScalingHealth.random.nextFloat();
            damageBoost = diffIncrease * SHMobs.damageBoostScale();
            // Clamp the value so it doesn't go over the maximum config.
            double max = SHMobs.maxDamageBoost();
            if (max > 0f) {
                damageBoost = MathHelper.clamp(damageBoost, 0, max);
            }

            ModifierHandler.addAttackDamage(entity, damageBoost, AttributeModifier.Operation.ADDITION);
        }
    }
}
