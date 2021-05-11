package net.silentchaos512.scalinghealth.utils;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.IDifficultyAffected;
import net.silentchaos512.scalinghealth.event.BlightHandler;
import net.silentchaos512.scalinghealth.event.CommonEvents;
import net.silentchaos512.scalinghealth.network.ClientBlightMessage;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.utils.config.EnabledFeatures;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;
import net.silentchaos512.scalinghealth.utils.config.SHMobs;
import net.silentchaos512.scalinghealth.utils.mode.MobHealthMode;
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

        return MathUtils.tryPercentage(ScalingHealth.RANDOM, chance);
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
            //TODO no good in code method for determining if an entity is a boss or not, switch to tag?
            if(entity instanceof WitherEntity || entity instanceof EnderDragonEntity) {
                ITextComponent blight = new TranslationTextComponent("misc.scalinghealth.blight", entity.getDisplayName()).deepCopy().mergeStyle(TextFormatting.DARK_PURPLE);
                entity.setCustomName(blight);
            }
        }

        //Get difficulty after making blight or not. This will determine if a blight's diff is multiplied
        final float difficulty = data.affectiveDifficulty();
        if(difficulty <= 0) return;

        // Random potion effect
        SHMobs.getMobEffects().forEach(c -> c.apply(entity, difficulty));

        if(EnabledFeatures.mobHpIncreaseEnabled()) {
            double healthBoost = difficulty;
            ModifiableAttributeInstance attributeMaxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
            double baseMaxHealth = attributeMaxHealth.getBaseValue();
            double healthMultiplier = isHostile
                    ? SHMobs.healthHostileMultiplier()
                    : SHMobs.healthPassiveMultiplier();

            healthBoost *= healthMultiplier;

            healthBoost += 2 * healthMultiplier * difficulty * ScalingHealth.RANDOM.nextFloat();;

            if(CommonEvents.spawnerSpawns.contains(entity.getUniqueID())){
                healthBoost *= SHMobs.spawnerModifier();
                CommonEvents.spawnerSpawns.remove(entity.getUniqueID());
            }

            // Apply extra health and damage.
            MobHealthMode mode = SHMobs.getHealthMode();
            ModifierHandler.setMaxHealth(entity, mode.getModifierHealth(healthBoost, baseMaxHealth), mode.getOp());
        }

        // Increase attack damage.
        if(EnabledFeatures.mobDamageIncreaseEnabled()) {
            double damageBoost;

            float diffIncrease = difficulty * ScalingHealth.RANDOM.nextFloat();
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
