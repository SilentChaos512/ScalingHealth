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
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkDirection;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.SHConfig;
import net.silentchaos512.scalinghealth.network.ClientLoginMessage;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.utils.config.EnabledFeatures;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;
import net.silentchaos512.scalinghealth.utils.config.SHMobs;
import net.silentchaos512.scalinghealth.utils.config.SHPlayers;
import net.silentchaos512.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public final class CommonEvents {
   public static List<UUID> spawnerSpawns = new ArrayList<>();
   private static boolean changedLevelThisTick = false;

   @SubscribeEvent
   public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
      PlayerEntity player = event.getPlayer();
      SHPlayers.getPlayerData(player).updateStats(player);

      if (!(player instanceof ServerPlayerEntity)) return;
      ServerPlayerEntity sp = (ServerPlayerEntity) event.getPlayer();
      ScalingHealth.LOGGER.debug("Sending login packet to player {}", player);
      ClientLoginMessage msg = new ClientLoginMessage(SHDifficulty.areaMode(), (float) SHDifficulty.maxValue());
      Network.channel.sendTo(msg, sp.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
   }

   @SubscribeEvent
   public static void onSpawn(LivingSpawnEvent.CheckSpawn event){
      if(!(event.getEntityLiving() instanceof MobEntity)) return;
      if(event.getSpawnReason() == SpawnReason.SPAWNER) spawnerSpawns.add(event.getEntityLiving().getUniqueID());
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onMobXPDropped(LivingExperienceDropEvent event) {
      LivingEntity entity = event.getEntityLiving();
      // Additional XP from all mobs.
      short difficulty = (short) SHDifficulty.areaDifficulty(entity.world, entity.getPosition());
      float multi = (float) (1.0f + SHMobs.xpBoost() * difficulty);

      float amount = event.getDroppedExperience();
      amount *= multi;

      // Additional XP from blights.
      if(entity instanceof MobEntity) {
         if (SHMobs.isBlight((MobEntity) entity)) {
            amount *= SHMobs.xpBlightBoost();
         }
      }
      event.setDroppedExperience(Math.round(amount));
   }

   @SubscribeEvent
   public static void playerTick(TickEvent.PlayerTickEvent event){
      if(event.phase == TickEvent.Phase.START) return;
      PlayerEntity player = event.player;

      if (player.world.isRemote) return;
      SHPlayers.getPlayerData(player).tick(player);

      if (changedLevelThisTick) {
         changedLevelThisTick = false;
         SHPlayers.getPlayerData(player).updateStats(player);
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onLevelChange(PlayerXpEvent.LevelChange event) {
      if(!EnabledFeatures.healthXpEnabled() || event.isCanceled()) return;
      changedLevelThisTick = true; //delay the player update until after the event, so that the Exp on the player was updated.
   }

   // TODO APRILS FOOLS?
//   public static void onPlayerDied(LivingDeathEvent event) {
//      if (event.getEntity() == null || !(event.getEntity() instanceof PlayerEntity))
//         return;
//
//      PlayerEntity player = (PlayerEntity) event.getEntity();
//      SoundUtils.play(player, Registration.PLAYER_DIED.get());
//   }

   @SubscribeEvent
   public static void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
      PlayerEntity player = event.getPlayer();
      if (!player.world.isRemote && SHConfig.CLIENT.warnWhenSleeping.get()) {
         double newDifficulty = SHDifficulty.diffOnPlayerSleep(player);

         if (!MathUtils.doublesEqual(SHDifficulty.getDifficultyOf(player), newDifficulty, 0.1)) {
            ScalingHealth.LOGGER.debug("old={}, new={}", SHDifficulty.getDifficultyOf(player), newDifficulty);
            player.sendMessage(new TranslationTextComponent("misc.scalinghealth.sleepWarning"), Util.DUMMY_UUID);
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
      PlayerEntity player = event.getPlayer();
      if (!player.world.isRemote && !event.updateWorld()) {
         SHDifficulty.setSourceDifficulty(player, SHDifficulty.diffOnPlayerSleep(player));
      }
   }
}
