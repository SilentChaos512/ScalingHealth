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
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.silentchaos512.scalinghealth.utils.Difficulty;
import net.silentchaos512.scalinghealth.lib.module.ModuleAprilTricks;

import javax.annotation.Nullable;

public class ScalingHealthCommonEvents {
    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        // Handle heart drops.
        // Was a player responsible for the death?
        /*
        EntityPlayer player = getPlayerThatCausedDeath(event.getSource());
        if (player == null || (player instanceof FakePlayer
                && !Config.FakePlayer.generateHearts)) {
            return;
        }

        EntityLivingBase killedEntity = event.getEntityLiving();
        if (!killedEntity.world.isRemote) {
            Random rand = ScalingHealth.random;
            int stackSize = 0;

            // Different drop rates for hostiles and passives.
            float dropRate = killedEntity instanceof IMob ? Config.Items.Heart.chanceHostile : Config.Items.Heart.chancePassive;
            if (killedEntity instanceof EntitySlime) {
                dropRate /= 6f;
            }

            ScalingHealth.logHelper.debug("heart drop rate for {} is {}", killedEntity.getName(), dropRate);

            // Basic heart drops for all mobs.
            if (event.isRecentlyHit() && rand.nextFloat() <= dropRate) {
                stackSize += 1;
            }

            // Heart drops for bosses.
            if (!killedEntity.isNonBoss()) {
                int min = Config.Items.Heart.bossMin;
                int max = Config.Items.Heart.bossMax;
                stackSize += min + rand.nextInt(max - min + 1);
            }

            if (stackSize > 0) {
                Item itemToDrop = Config.Items.Heart.dropShardsInstead ? ModItems.crystalShard
                        : ModItems.heart;
                killedEntity.dropItem(itemToDrop, stackSize);
            }
        }
        */
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onXPDropped(LivingExperienceDropEvent event) {
        /*
        EntityLivingBase entityLiving = event.getEntityLiving();

        // Additional XP from all mobs.
        short difficulty = entityLiving.getEntityData()
                .getShort(DifficultyHandler.NBT_ENTITY_DIFFICULTY);
        float multi = 1.0f + Config.Mob.xpBoost * difficulty;

        float amount = event.getDroppedExperience();
        amount *= multi;

        // Additional XP from blights.
        if (BlightHandler.isBlight(entityLiving)) {
            amount *= Config.Mob.Blight.xpMultiplier;
        }

        event.setDroppedExperience(Math.round(amount));
        */
    }

    /**
     * Get the player that caused a mob's death. Could be a FakePlayer or null.
     *
     * @return The player that caused the damage, or the owner of the tamed animal that caused the
     * damage.
     */
    @Nullable
    private EntityPlayer getPlayerThatCausedDeath(DamageSource source) {
        if (source == null) {
            return null;
        }

        // Player is true source.
        Entity entitySource = source.getTrueSource();
        if (entitySource instanceof EntityPlayer) {
            return (EntityPlayer) entitySource;
        }

        // Player's pet is true source.
        boolean isTamedAnimal = entitySource instanceof EntityTameable
                && ((EntityTameable) entitySource).isTamed();
        if (entitySource instanceof EntityTameable) {
            EntityTameable tamed = (EntityTameable) entitySource;
            if (tamed.isTamed() && tamed.getOwner() instanceof EntityPlayer) {
                return (EntityPlayer) tamed.getOwner();
            }
        }

        // No player responsible.
        return null;
    }

    @SubscribeEvent
    public void onPlayerDied(LivingDeathEvent event) {
        if (event.getEntity() == null || !(event.getEntity() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntity();

        if (ModuleAprilTricks.instance.isEnabled() && ModuleAprilTricks.instance.isRightDay()) {
//            ScalingHealth.proxy.playSoundOnClient(player, ModSounds.PLAYER_DIED, 0.6f, 1f);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        /*
        // Set player health correctly after respawn.
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            PlayerData data = SHPlayerDataHandler.get(player);
            if (data == null) return;

            // Lose health on death?
            if (Config.Player.Health.lostOnDeath > 0 && !event.isEndConquered()) {
                float newHealth = data.getMaxHealth() - Config.Player.Health.lostOnDeath;
                float startHealth = Config.Player.Health.startingHealth;
                data.setMaxHealth(newHealth < startHealth ? startHealth : newHealth);
            }

            // Lose difficulty on death?
            if (!event.isEndConquered()) {
                double currentDifficulty = data.getDifficulty();
                double newDifficulty = MathHelper.clamp(
                        currentDifficulty - Config.Difficulty.lostOnDeath,
                        Config.Difficulty.minValue, Config.Difficulty.maxValue);
                data.setDifficulty(newDifficulty);
            }

            // Apply health modifier
            if (Config.Player.Health.allowModify) {
                float health = player.getHealth();
                float maxHealth = data.getMaxHealth();
                ModifierHandler.setMaxHealth(player, maxHealth, 0);
                if (health != maxHealth && maxHealth > 0) {
                    player.setHealth(player.getMaxHealth());
                }
            }
        }
        */
    }

    @SubscribeEvent
    public void onPlayerJoinedServer(PlayerLoggedInEvent event) {
        /*
        // Sync player data and set health.
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            PlayerData data = SHPlayerDataHandler.get(player);

            // Resets, based on config?
            Calendar today = Calendar.getInstance();
            Calendar lastTimePlayed = data.getLastTimePlayed();

            if (Config.Difficulty.DIFFFICULTY_RESET_TIME.shouldReset(today, lastTimePlayed)) {
                ScalingHealth.logHelper.info("Reset player {}'s difficulty to {}", player.getName(), (int) Config.Difficulty.startValue);
                ChatHelper.sendMessage(player, "[Scaling Health] Your difficulty has been reset.");
                data.setDifficulty(Config.Difficulty.startValue);
            }
            if (Config.Player.Health.resetTime.shouldReset(today, lastTimePlayed)) {
                data.setMaxHealth(Config.Player.Health.startingHealth);
                ScalingHealth.logHelper.info("Reset player {}'s health to {}", player.getName(), Config.Player.Health.startingHealth);
                ChatHelper.sendMessage(player, "[Scaling Health] Your health has been reset.");
            }

            data.getLastTimePlayed().setTime(today.getTime());

            // Apply health modifier
            if (Config.Player.Health.allowModify) {
                float maxHealth = data.getMaxHealth();
                ModifierHandler.setMaxHealth(player, maxHealth, 0);
            }
        }

        if (ModuleAprilTricks.instance.isEnabled() && ModuleAprilTricks.instance.isRightDay()) {
            ChatHelper.sendMessage(event.player,
                    TextFormatting.RED + "[Scaling Health] It's April Fool's time... hehehe.");
        }
        */
    }

    @SubscribeEvent
    public void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        /*
        if (!event.getEntityPlayer().world.isRemote && Config.Client.Difficulty.warnWhenSleeping && Config.Difficulty.forSleeping != 0f) {
            ChatHelper.translate(event.getEntityPlayer(), ScalingHealth.i18n.getKey("misc", "sleepWarning"));
        }
        */
    }

    @SubscribeEvent
    public void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!event.getEntityPlayer().world.isRemote && !event.updateWorld()) {
            EntityPlayer player = event.getEntityPlayer();
            // TODO: Sleep difficulty change config
            Difficulty.source(player).addDifficulty(0);

            // TODO: World difficulty increase?
        }
    }

    /*
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(ScalingHealth.MOD_ID)) {
            Config.INSTANCE.load();
            Config.INSTANCE.save();
        }
    }
    */
}
