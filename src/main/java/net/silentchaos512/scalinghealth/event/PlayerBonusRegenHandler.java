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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.RegenConfig;
import net.silentchaos512.scalinghealth.utils.SHPlayers;

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
        RegenConfig config = SHPlayers.getRegenConfig();

        UUID uuid = player.getUniqueID();

        // Add player timer if needed.
        if (!TIMERS.containsKey(uuid)) {
            TIMERS.put(uuid, config.getInitialDelay());
        }

        if (config.isActive(player)) {
            // Tick timer, heal player and reset on 0.
            int timer = TIMERS.get(uuid);
            if (--timer <= 0) {
                player.heal(config.getHealTickAmount(player));
                player.addExhaustion(config.getExhaustion());
                timer = config.getTickDelay();
            }
            TIMERS.put(uuid, timer);
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (!entity.world.isRemote && entity instanceof PlayerEntity) {
            TIMERS.put(entity.getUniqueID(), SHPlayers.getRegenConfig().getInitialDelay());
        }
    }
}
