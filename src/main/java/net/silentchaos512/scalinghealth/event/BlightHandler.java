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

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkDirection;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.network.ClientBlightMessage;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.utils.SHDifficulty;
import net.silentchaos512.scalinghealth.utils.SHMobs;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public final class BlightHandler {
    private BlightHandler() {}

    public static void applyBlightPotionEffects(MobEntity entityLiving) {
        Config.GENERAL.mobs.blightPotions.applyAll(entityLiving);
    }

    private static void notifyPlayers(ITextComponent deathMessage, MobEntity blight, PlayerEntity slayer){
        if (deathMessage instanceof TranslationTextComponent) {
            // Assuming arguments are the same as in DamageSource#getDeathMessage
            // May fail with some modded damage sources, but should be fine in most cases
            TranslationTextComponent original = (TranslationTextComponent) deathMessage;

            StringTextComponent s = new StringTextComponent("Blight " + blight.getName().getString());
            s.setStyle(Style.EMPTY.setFormatting(TextFormatting.DARK_PURPLE));

            TranslationTextComponent newMessage = new TranslationTextComponent(original.getKey(), s);
            StringTextComponent almostFinalMessage = new StringTextComponent(newMessage.getString());
            String message = newMessage.getString();

            if(message.contains("drowned")){
                if(message.startsWith("Blight Squid")){
                    almostFinalMessage = new StringTextComponent(almostFinalMessage.getString() + "... again");
                }
                else
                    almostFinalMessage = new StringTextComponent(almostFinalMessage.getString() + "... gg");
            } else if(message.contains("suffocated in a wall")){
                almostFinalMessage = new StringTextComponent(almostFinalMessage.getString() + " *slow clap*");
            }

            StringTextComponent finalMessage = almostFinalMessage;

            if(slayer != null)  {
                if(almostFinalMessage.getString().contains("  "))
                    finalMessage = new StringTextComponent(almostFinalMessage.getString().replace("  ", " " + slayer.getName().getString() + " ")) ;
                else
                    finalMessage = new StringTextComponent(almostFinalMessage.getString() + slayer.getName().getString());
            }

            for (PlayerEntity p : blight.world.getPlayers())
                p.sendMessage(finalMessage, Util.DUMMY_UUID);
        }
    }

    // **********
    // * Events *
    // **********

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlightKilled(LivingDeathEvent event) {
        if(!(event.getEntityLiving() instanceof MobEntity)) return;

        MobEntity blight = (MobEntity) event.getEntityLiving();
        if (event.getSource() == null || !SHMobs.isBlight(blight) || event.getEntity().world.isRemote)
            return;

        Entity entitySource = event.getSource().getTrueSource();
        boolean isTamedAnimal = entitySource instanceof TameableEntity && ((TameableEntity) entitySource).isTamed();
        if (entitySource instanceof PlayerEntity || isTamedAnimal) {
            // Killed by a player or a player's pet.
            PlayerEntity player;
            LivingEntity actualKiller;
            if (isTamedAnimal) {
                player = (PlayerEntity) ((TameableEntity) entitySource).getOwner();
                actualKiller = (MobEntity) entitySource;
            } else {
                actualKiller = player = (PlayerEntity) entitySource;
            }

            // Tell all players that the blight was killed.
            if (SHMobs.notifyOnDeath() && player != null) {
                ScalingHealth.LOGGER.info("Blight {} was killed by {}", blight.getName().getString(), actualKiller.getName().getString());
                notifyPlayers(event.getSource().getDeathMessage(blight), blight, player);
            }
        } else {
            // Killed by something else.
            // Tell all players that the blight died.
            if (SHMobs.notifyOnDeath())
                ScalingHealth.LOGGER.info("Blight {} has died", blight.getName().getString());
                notifyPlayers(event.getSource().getDeathMessage(blight), blight, null);
        }
    }

    @SubscribeEvent
    public static void startTrackingBlight(PlayerEvent.StartTracking event){
        if(event.getTarget() instanceof MobEntity) {
            MobEntity mob = (MobEntity) event.getTarget();
            if(SHDifficulty.affected(mob).isBlight()) {
                ServerPlayerEntity sp = (ServerPlayerEntity) event.getPlayer();
                ClientBlightMessage msg = new ClientBlightMessage(mob.getEntityId());
                Network.channel.sendTo(msg, sp.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }

    @SubscribeEvent
    public static void onBlightUpdate(LivingUpdateEvent event) {
        LivingEntity blight = event.getEntityLiving();
        if (!blight.world.isRemote && blight instanceof MobEntity && SHMobs.isBlight((MobEntity) blight) && blight.world.getGameTime() % 1000 == 0) {
            applyBlightPotionEffects((MobEntity) blight);
        }
    }
}
