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

import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PetEventHandler {
    public static PetEventHandler INSTANCE = new PetEventHandler();

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        /*
        final int regenDelay = Config.PET_REGEN_DELAY;
        if (regenDelay <= 0) {
            return;
        }

        EntityLivingBase entity = event.getEntityLiving();
        if (entity != null && !entity.world.isRemote) {
            boolean isTamed = entity instanceof EntityTameable && ((EntityTameable) entity).isTamed();
            boolean isRegenTime = entity.hurtResistantTime <= 0 && entity.ticksExisted % regenDelay == 0;
            if (isTamed && isRegenTime) {
                entity.heal(2f);
            }
        }
        */
    }
}
