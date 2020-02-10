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
import net.minecraft.entity.passive.TameableEntity;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PetEventHandler {
    //TODO add option to add heart crystals to pets.
    public static PetEventHandler INSTANCE = new PetEventHandler();
    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        final double regenDelay = Config.get(event.getEntity()).pets.regenDelay.get();
        if (regenDelay <= 0) {
            return;
        }

        LivingEntity entity = event.getEntityLiving();
        if (entity != null && !entity.world.isRemote) {
            boolean fullHp = entity.getHealth() == entity.getMaxHealth();
            boolean isTamed = entity instanceof TameableEntity && ((TameableEntity) entity).isTamed();
            boolean isRegenTime = entity.hurtResistantTime <= 0 && entity.ticksExisted % regenDelay == 0;
            if (isTamed && isRegenTime && !fullHp) {
                entity.heal(2f);
                ScalingHealth.LOGGER.debug("Healing Tamed Animal");
            }
        }
    }
}
