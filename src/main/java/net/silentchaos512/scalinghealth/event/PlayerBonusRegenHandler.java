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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.silentchaos512.lib.util.MathUtils;
import net.silentchaos512.scalinghealth.config.Config;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public final class PlayerBonusRegenHandler {
    private static final Map<String, Integer> TIMERS = new HashMap<>();

    private PlayerBonusRegenHandler() {}

    static int getTimerForPlayer(EntityPlayer player) {
        if (player == null || !TIMERS.containsKey(player.getName()))
            return -1;
        return TIMERS.get(player.getName());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (event.side == Side.CLIENT || !Config.Player.BonusRegen.enabled)
            return;

        EntityPlayer player = event.player;
        int health = (int) Math.ceil(player.getHealth());
        if (health >= player.getMaxHealth()) return;

        String name = player.getName();

        // Add player timer if needed.
        if (!TIMERS.containsKey(name))
            TIMERS.put(name, Config.Player.BonusRegen.initialDelay);

        int foodLevel = player.getFoodStats().getFoodLevel();

        boolean foodLevelOk = MathUtils.inRangeInclusive(foodLevel,
                Config.Player.BonusRegen.minFood, Config.Player.BonusRegen.maxFood);
        boolean healthLevelOk = MathUtils.inRangeExclusive(health,
                Config.Player.BonusRegen.minHealth, Config.Player.BonusRegen.maxHealth);

        if (foodLevelOk && healthLevelOk) {
            // Tick timer, heal player and reset on 0.
            int timer = TIMERS.get(name);
            if (--timer <= 0) {
                player.heal(1f);
                player.addExhaustion(Config.Player.BonusRegen.exhaustion);
                timer = Config.Player.BonusRegen.delay;
            }
            TIMERS.put(name, timer);
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        EntityLivingBase entityLiving = event.getEntityLiving();
        if (!entityLiving.world.isRemote && entityLiving instanceof EntityPlayer)
            TIMERS.put(entityLiving.getName(), Config.Player.BonusRegen.initialDelay);
    }
}
