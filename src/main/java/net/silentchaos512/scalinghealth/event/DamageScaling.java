/*
 * Scaling Health
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.scalinghealth.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.DimensionConfig;
import net.silentchaos512.scalinghealth.lib.EntityGroup;
import net.silentchaos512.scalinghealth.utils.Difficulty;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class DamageScaling {
    private static final Marker MARKER = MarkerManager.getMarker("DamageScaling");
    private static final String[] SOURCES_DEFAULT = {"inFire", "lightningBolt", "onFire", "lava", "hotFloor", "inWall",
            "cramming", "drown", "starve", "cactus", "fall", "flyIntoWall", "outOfWorld", "generic", "magic", "wither",
            "anvil", "fallingBlock", "dragonBreath", "fireworks"};

    public static final DamageScaling INSTANCE = new DamageScaling();

    private final Set<UUID> entityAttackedThisTick = new HashSet<>();

    private DamageScaling() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerHurt(LivingAttackEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.world.isRemote) return;
        // Entity invulnerable?
        if (entity.isInvulnerableTo(event.getSource()) || entity.hurtResistantTime > entity.maxHurtResistantTime / 2)
            return;

        // Check entity has already been processed from original event, or is not allowed to be affected
        if (entityAttackedThisTick.contains(entity.getUniqueID()) || !EntityGroup.from(entity).isAffectedByDamageScaling(entity))
            return;

        DamageSource source = event.getSource();
        if (source == null) return;

        // Get scaling factor from map, if it exists. Otherwise, use the generic scale.
        float scale = (float) Config.get(entity).damageScaling.getScale(source.getDamageType());

        // Get the amount of the damage to affect. Can be many times the base value.
        final float affectedAmount = (float) getEffectScale(entity);

        // Calculate damage to add to the original.
        final float original = event.getAmount();
        final float change = scale * affectedAmount * original;

        if (change > 0.0001f) {
            final float newAmount = makeSane(event.getAmount() + change);

            event.setCanceled(true);
            entityAttackedThisTick.add(entity.getUniqueID());
            entity.attackEntityFrom(event.getSource(), newAmount);

            if (Config.COMMON.debugLogScaledDamage.get()) {
                ScalingHealth.LOGGER.info(MARKER, "{} on {}: {} -> {} (scale={}, affected={}, change={})",
                        source.damageType, entity.getScoreboardName(), original, newAmount, scale, affectedAmount, change);
            }
        }
    }

    private static double getEffectScale(LivingEntity entity) {
        DimensionConfig config = Config.get(entity);
        Mode mode = config.damageScaling.mode.get();
        switch (mode) {
            case AREA_DIFFICULTY:
                return Difficulty.areaDifficulty(entity.world, entity.getPosition()) * config.damageScaling.difficultyWeight.get();
            case MAX_HEALTH:
                double baseHealth = entity instanceof PlayerEntity
                        ? config.player.startingHealth.get()
                        : entity.getAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue();
                return (entity.getMaxHealth() - baseHealth) / baseHealth;
            case DIFFICULTY:
                return Difficulty.getDifficultyOf(entity) * config.damageScaling.difficultyWeight.get();
            default:
                throw new IllegalStateException("Unknown damage scaling mode: " + mode);
        }
    }

    private static float makeSane(float scaledAmount) {
        // Clamp scaled damage to sane values (non-negative and finite)
        if (scaledAmount < 0)
            return 0;
        if (!Float.isFinite(scaledAmount))
            return Float.MAX_VALUE;
        return scaledAmount;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        entityAttackedThisTick.clear();
    }

    public enum Mode {
        MAX_HEALTH, DIFFICULTY, AREA_DIFFICULTY;
    }
}