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
import net.minecraft.util.ActionResultType;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.objects.item.HeartCrystal;
import net.silentchaos512.scalinghealth.resources.mechanics.SHMechanicListener;
import net.silentchaos512.scalinghealth.utils.config.EnabledFeatures;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PetEventHandler {
    @SubscribeEvent
    public static void onLivingUpdate(LivingUpdateEvent event) {
        double regenDelay = SHMechanicListener.getMobMechanics().pets.petsRegenDelay;
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
            }
        }
    }

    @SubscribeEvent
    public static void onPetInteraction(PlayerInteractEvent.EntityInteractSpecific event){
        if(!(event.getItemStack().getItem() instanceof HeartCrystal) ||
                !(event.getTarget() instanceof TameableEntity)       ||
                !EnabledFeatures.petBonusHpEnabled())
            return;

        TameableEntity pet = (TameableEntity) event.getTarget();
        if(!pet.isTamed()) {
            return;
        }
        if(pet.world.isRemote){
            event.setCancellationResult(ActionResultType.SUCCESS);
            event.setCanceled(true);
            return;
        }
        HeartCrystal heart = (HeartCrystal) event.getItemStack().getItem();
        heart.increasePetHp(event.getPlayer(), pet, event.getItemStack());
    }
}