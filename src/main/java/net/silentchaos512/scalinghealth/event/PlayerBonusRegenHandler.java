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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public final class PlayerBonusRegenHandler {
    private static final Map<UUID, Integer> TIMERS = new HashMap<>();

    private PlayerBonusRegenHandler() {}

    static int getTimerForPlayer(EntityPlayer player) {
        if (player == null) return -1;

        UUID uuid = player.getUniqueID();
        return TIMERS.getOrDefault(uuid, -1);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        // FIXME
        /*
        if (event.side == LogicalSide.CLIENT || !Config.Player.BonusRegen.enabled)
            return;

        EntityPlayer player = event.player;
        int health = (int) Math.ceil(player.getHealth());
        if (health >= player.getMaxHealth()) return;

        UUID uuid = player.getUniqueID();

        // Add player timer if needed.
        if (!TIMERS.containsKey(uuid))
            TIMERS.put(uuid, Config.Player.BonusRegen.initialDelay);

        int foodLevel = player.getFoodStats().getFoodLevel();

        boolean foodLevelOk = MathUtils.inRangeInclusive(foodLevel,
                Config.Player.BonusRegen.minFood, Config.Player.BonusRegen.maxFood);
        boolean healthLevelOk = MathUtils.inRangeExclusive(health,
                Config.Player.BonusRegen.minHealth, Config.Player.BonusRegen.maxHealth);

        if (foodLevelOk && healthLevelOk) {
            // Tick timer, heal player and reset on 0.
            int timer = TIMERS.get(uuid);
            if (--timer <= 0) {
                player.heal(1f);
                player.addExhaustion(Config.Player.BonusRegen.exhaustion);
                timer = Config.Player.BonusRegen.delay;
            }
            TIMERS.put(uuid, timer);
        }
        */
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
//        EntityLivingBase entityLiving = event.getEntityLiving();
//        if (!entityLiving.world.isRemote && entityLiving instanceof EntityPlayer)
//            TIMERS.put(entityLiving.getName(), Config.Player.BonusRegen.initialDelay);
    }
}
