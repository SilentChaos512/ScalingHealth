package net.silentchaos512.scalinghealth.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.network.PacketDistributor;
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

    public static void process(Mob entity, IDifficultyAffected data) {
        if (!entity.isAlive()) return;
        setEntityProperties(entity, data, shouldBecomeBlight(entity, data.getDifficulty()));
    }

    public static boolean shouldBecomeBlight(Mob entity, float difficulty) {
        if (!SHMobs.canBecomeBlight(entity))    return false;

        double chance = getBlightChance(difficulty);
        if(chance == 1)    return true;

        return MathUtils.tryPercentage(ScalingHealth.RANDOM, chance);
    }

    private static double getBlightChance(float difficulty) {
        return SHMobs.blightChance() * difficulty / SHDifficulty.maxValue();
    }

    public static void setEntityProperties(Mob entity, IDifficultyAffected data, boolean makeBlight) {
        if (!entity.isAlive()) return;

        boolean isHostile = entity instanceof Enemy;

        if (makeBlight) {
            data.setIsBlight(true);
            ClientBlightMessage msg = new ClientBlightMessage(entity.getId());
            Network.channel.send(PacketDistributor.TRACKING_ENTITY.with(()->entity), msg);

            BlightHandler.applyBlightPotionEffects(entity);
            //TODO no good in code method for determining if an entity is a boss or not, switch to tag?
            if(entity instanceof WitherBoss || entity instanceof EnderDragon) {
                Component blight = new TranslatableComponent("misc.scalinghealth.blight", entity.getDisplayName()).copy().withStyle(ChatFormatting.DARK_PURPLE);
                entity.setCustomName(blight);
            }
        }

        //Get difficulty after making blight or not. This will determine if a blight's diff is multiplied
        final float difficulty = data.affectiveDifficulty();
        if(difficulty <= 0) return;

        // Random potion effect
        SHMobs.getMobEffects().forEach(c -> c.tryApply(entity, difficulty));

        if(EnabledFeatures.mobHpIncreaseEnabled()) {
            double healthBoost = difficulty;

            double healthMultiplier = isHostile
                    ? SHMobs.healthHostileMultiplier()
                    : SHMobs.healthPassiveMultiplier();

            healthBoost *= healthMultiplier;

            //TODO test... wtf was i doing here?
            //healthBoost += 2 * healthMultiplier * difficulty * ScalingHealth.RANDOM.nextFloat();

            if(CommonEvents.spawnerSpawns.contains(entity.getUUID())){
                healthBoost *= SHMobs.spawnerModifier();
                CommonEvents.spawnerSpawns.remove(entity.getUUID());
            }

            // Apply extra health and damage.
            MobHealthMode mode = SHMobs.getHealthMode();
            ModifierHandler.setMaxHealth(entity, mode.getModifierHealth(healthBoost, entity.getAttribute(Attributes.MAX_HEALTH).getBaseValue()), mode.getOp());
        }

        // Increase attack damage.
        if(EnabledFeatures.mobDamageIncreaseEnabled()) {
            double damageBoost;

            float diffIncrease = difficulty * ScalingHealth.RANDOM.nextFloat();
            damageBoost = diffIncrease * SHMobs.damageBoostScale();
            // Clamp the value so it doesn't go over the maximum config.
            double max = SHMobs.maxDamageBoost();
            if (max > 0f) {
                damageBoost = Mth.clamp(damageBoost, 0, max);
            }

            ResourceLocation loc = entity.getType().getRegistryName();
            if (loc != null && !SHDifficulty.getDamageBlacklistedMods().contains(loc.getNamespace()))
                ModifierHandler.addAttackDamage(entity, damageBoost, AttributeModifier.Operation.ADDITION);
        }
    }
}
