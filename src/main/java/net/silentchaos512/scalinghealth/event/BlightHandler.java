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

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.network.ClientBlightMessage;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.resources.mechanics.SHMechanicListener;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;
import net.silentchaos512.scalinghealth.utils.config.SHMobs;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public final class BlightHandler {
    private BlightHandler() {}

    public static void applyBlightPotionEffects(Mob entity) {
        SHMechanicListener.getMobMechanics().blight.blightEffects.forEach(e -> e.apply(entity, SHDifficulty.getDifficultyOf(entity)));
    }

    private static void notifyPlayers(Component deathMessage, Mob blight, Player slayer){
        if (deathMessage instanceof TranslatableComponent) {
            // Assuming arguments are the same as in DamageSource#getDeathMessage
            // May fail with some modded damage sources, but should be fine in most cases
            TranslatableComponent original = (TranslatableComponent) deathMessage;

            TextComponent s = new TextComponent("Blight " + blight.getName().getString());
            s.setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE));

            TranslatableComponent newMessage = new TranslatableComponent(original.getKey(), s);
            TextComponent almostFinalMessage = new TextComponent(newMessage.getString());
            String message = newMessage.getString();

            if(message.contains("drowned")){
                if(message.startsWith("Blight Squid")){
                    almostFinalMessage = new TextComponent(almostFinalMessage.getString() + "... again");
                }
                else
                    almostFinalMessage = new TextComponent(almostFinalMessage.getString() + "... gg");
            } else if(message.contains("suffocated in a wall")){
                almostFinalMessage = new TextComponent(almostFinalMessage.getString() + " *slow clap*");
            }

            TextComponent finalMessage = almostFinalMessage;

            if(slayer != null)  {
                if(almostFinalMessage.getString().contains("  "))
                    finalMessage = new TextComponent(almostFinalMessage.getString().replace("  ", " " + slayer.getName().getString() + " ")) ;
                else
                    finalMessage = new TextComponent(almostFinalMessage.getString() + slayer.getName().getString());
            }

            for (Player p : blight.level.players())
                p.sendMessage(finalMessage, Util.NIL_UUID);
        }
    }

    // **********
    // * Events *
    // **********

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlightKilled(LivingDeathEvent event) {
        if(!(event.getEntityLiving() instanceof Mob)) return;

        Mob blight = (Mob) event.getEntityLiving();
        if (event.getSource() == null || !SHMobs.isBlight(blight) || event.getEntity().level.isClientSide)
            return;

        Entity entitySource = event.getSource().getEntity();
        boolean isTamedAnimal = entitySource instanceof TamableAnimal && ((TamableAnimal) entitySource).isTame();
        if (entitySource instanceof Player || isTamedAnimal) {
            // Killed by a player or a player's pet.
            Player player;
            LivingEntity actualKiller;
            if (isTamedAnimal) {
                player = (Player) ((TamableAnimal) entitySource).getOwner();
                actualKiller = (Mob) entitySource;
            } else {
                actualKiller = player = (Player) entitySource;
            }

            // Tell all players that the blight was killed.
            if (SHMobs.notifyBlightDeath() && player != null) {
                ScalingHealth.LOGGER.info("Blight {} was killed by {}", blight.getName().getString(), actualKiller.getName().getString());
                notifyPlayers(event.getSource().getLocalizedDeathMessage(blight), blight, player);
            }
        } else {
            // Killed by something else.
            // Tell all players that the blight died.
            if (SHMobs.notifyBlightDeath())
                ScalingHealth.LOGGER.info("Blight {} has died", blight.getName().getString());
                notifyPlayers(event.getSource().getLocalizedDeathMessage(blight), blight, null);
        }
    }

    @SubscribeEvent
    public static void startTrackingBlight(PlayerEvent.StartTracking event){
        if(event.getTarget() instanceof Mob) {
            Mob mob = (Mob) event.getTarget();
            if(SHDifficulty.affected(mob).isBlight()) {
                ServerPlayer sp = (ServerPlayer) event.getPlayer();
                ClientBlightMessage msg = new ClientBlightMessage(mob.getId());
                Network.channel.sendTo(msg, sp.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }

    @SubscribeEvent
    public static void onBlightUpdate(LivingUpdateEvent event) {
        LivingEntity blight = event.getEntityLiving();
        if (!blight.level.isClientSide && blight instanceof Mob && SHMobs.isBlight((Mob) blight) && blight.level.getGameTime() % 1000 == 0) {
            applyBlightPotionEffects((Mob) blight);
        }
    }
}
