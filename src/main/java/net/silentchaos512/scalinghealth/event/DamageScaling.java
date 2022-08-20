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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.SHConfig;
import net.silentchaos512.scalinghealth.resources.mechanics.DamageScalingMechanics;
import net.silentchaos512.scalinghealth.resources.mechanics.SHMechanics;
import net.silentchaos512.scalinghealth.utils.EntityGroup;
import net.silentchaos512.scalinghealth.utils.config.EnabledFeatures;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;
import net.silentchaos512.scalinghealth.utils.config.SHPlayers;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public final class DamageScaling {
    private static final Marker MARKER = MarkerManager.getMarker("DamageScaling");

    private static final Set<UUID> ENTITY_ATTACKED_THIS_TICK = new HashSet<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityHurt(LivingAttackEvent event) {
        if(!EnabledFeatures.mobDamageScalingEnabled() && !EnabledFeatures.playerDamageScalingEnabled()) return;
        LivingEntity entity = event.getEntity();
        if (entity.level.isClientSide) return;
        // Entity invulnerable?
        if (entity.isInvulnerableTo(event.getSource()) || entity.invulnerableTime > entity.invulnerableDuration / 2)
            return;

        // Check entity has already been processed from original event, or is not allowed to be affected
        if (ENTITY_ATTACKED_THIS_TICK.contains(entity.getUUID()) || !EntityGroup.from(entity).isAffectedByDamageScaling())
            return;

        DamageSource source = event.getSource();
        if (source == null) return;

        // Get scaling factor from map, if it exists. Otherwise, use the generic scale.
        float scale = SHMechanics.getMechanics().damageScalingMechanics().scales
                .stream()
                .filter(p -> p.getFirst().contains(source.getMsgId()))
                .map(Pair::getSecond)
                .reduce((s1, s2) -> s1 * s2)
                .orElseGet(() -> SHMechanics.getMechanics().damageScalingMechanics().genericScale)
                .floatValue();

        // Get the amount of the damage to affect. Can be many times the base value.
        final float affectedAmount = (float) getEffectScale(entity);

        // Calculate damage to add to the original.
        final float original = event.getAmount();
        final float change = scale * affectedAmount * original;

        if (change > 0.0001f) {
            final float newAmount = makeSane(event.getAmount() + change);

            event.setCanceled(true);
            ENTITY_ATTACKED_THIS_TICK.add(entity.getUUID());
            entity.hurt(event.getSource(), newAmount);

            if (SHConfig.SERVER.debugLogScaledDamage.get()) {
                ScalingHealth.LOGGER.debug(MARKER, "{} on {}: {} -> {} (scale={}, affected={}, change={})",
                        source.msgId, entity.getScoreboardName(), original, newAmount, scale, affectedAmount, change);
            }
        }
    }

    private static double getEffectScale(LivingEntity entity) {
        DamageScalingMechanics config = SHMechanics.getMechanics().damageScalingMechanics();
        Mode mode = config.mode;
        switch (mode) {
            case AREA_DIFFICULTY:
                return SHDifficulty.areaDifficulty(entity.level, entity.blockPosition()) * config.difficultyWeight;
            case MAX_HEALTH:
                AttributeInstance attr = entity.getAttribute(Attributes.MAX_HEALTH);
                if (attr == null) {
                    ScalingHealth.LOGGER.warn("Living Entity {} has no max health attribute", ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()));
                    return 1;
                }
                double baseHealth = entity instanceof Player
                        ? SHPlayers.startingHealth()
                        : attr.getBaseValue();
                return (entity.getMaxHealth() - baseHealth) / baseHealth;
            case DIFFICULTY:
                return SHDifficulty.getDifficultyOf(entity) * config.difficultyWeight;
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
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        ENTITY_ATTACKED_THIS_TICK.clear();
    }

    public enum Mode implements StringRepresentable {
        MAX_HEALTH,
        DIFFICULTY,
        AREA_DIFFICULTY;

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        @Override
        public String getSerializedName() {
            return name();
        }
    }
}