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
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.resources.mechanics.PlayerMechanics;
import net.silentchaos512.scalinghealth.resources.mechanics.SHMechanicListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public final class PlayerBonusRegenHandler {
    private static final Map<UUID, Integer> TIMERS = new HashMap<>();

    private PlayerBonusRegenHandler() {}

    public static int getTimerForPlayer(PlayerEntity player) {
        if (player == null) return -1;

        UUID uuid = player.getUniqueID();
        return TIMERS.getOrDefault(uuid, -1);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.CLIENT) return;

        PlayerEntity player = event.player;
        PlayerMechanics.RegenMechanics config = SHMechanicListener.getPlayerMechanics().regenMechanics;

        UUID uuid = player.getUniqueID();

        // Add player timer if needed.
        if (!TIMERS.containsKey(uuid)) {
            TIMERS.put(uuid, (int) (config.initialDelay * 20));
        }

        if (isActive(player)) {
            // Tick timer, heal player and reset on 0.
            int timer = TIMERS.get(uuid);
            if (--timer <= 0) {
                player.heal(getHealTickAmount(player));
                player.addExhaustion((float) config.exhaustion);
                timer = (int) (20 * config.tickDelay);
            }
            TIMERS.put(uuid, timer);
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (!entity.world.isRemote && entity instanceof PlayerEntity) {
            TIMERS.put(entity.getUniqueID(), (int) (SHMechanicListener.getPlayerMechanics().regenMechanics.initialDelay * 20));
        }
    }

    private static float getHealTickAmount(LivingEntity entity) {
        if (SHMechanicListener.getPlayerMechanics().regenMechanics.proportionaltoMaxHp) {
            ModifiableAttributeInstance attr = entity.getAttribute(Attributes.MAX_HEALTH);
            if (attr == null) {
                ScalingHealth.LOGGER.warn("LivingEntity {} does not have a max hp attribute!", entity.getType().getRegistryName());
                return 0;
            }
            double base = attr.getBaseValue();
            double max = attr.getValue();
            return (float) (max / base);
        }
        return 1;
    }

    private static boolean isActive(LivingEntity entity) {
        PlayerMechanics.RegenMechanics config = SHMechanicListener.getPlayerMechanics().regenMechanics;
        if (!entity.isAlive() || entity.getHealth() >= entity.getMaxHealth()) {
            return false;
        }

        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            int food = player.getFoodStats().getFoodLevel();
            if (food < config.minFood || food > config.maxFood) {
                return false;
            }
        }

        float health = entity.getHealth();
        return health >= config.regenMinHealth && health <= config.regenMaxHealth;
    }
}
