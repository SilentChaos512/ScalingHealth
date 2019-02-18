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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.lib.MobType;
import net.silentchaos512.scalinghealth.lib.module.ModuleAprilTricks;
import net.silentchaos512.scalinghealth.network.ClientLoginMessage;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.utils.Difficulty;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public final class ScalingHealthCommonEvents {
    private ScalingHealthCommonEvents() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        if (!(event.getPlayer() instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.getPlayer();
        World world = player.world;
        ScalingHealth.LOGGER.info("Sending login packet to player {}", player);
        ClientLoginMessage msg = new ClientLoginMessage(Difficulty.areaMode(world), (float) Difficulty.maxValue(world));
        Network.channel.sendTo(msg, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof EntityLivingBase)) return;

        EntityLivingBase entity = (EntityLivingBase) event.getEntity();
        World world = entity.world;
        if (world.isRemote) return;
        MinecraftServer server = world.getServer();
        if (server == null) return;

        // Mob loot disabled?
        if (!world.getGameRules().getBoolean("doMobLoot")) return;

        EntityPlayer player = getPlayerThatCausedDeath(event.getSource());

        // Get the bonus drops loot table for this mob type
        Optional<ResourceLocation> tableName = MobType.from(entity, true).getBonusDropsLootTable();
        if (!tableName.isPresent()) return;

        LootTable lootTable = server.getLootTableManager().getLootTableFromLocation(tableName.get());
        LootContext.Builder contextBuilder = new LootContext.Builder((WorldServer) world)
                .withDamageSource(event.getSource())
                .withLootedEntity(entity);
        if (player != null) contextBuilder.withLuck(player.getLuck()).withPlayer(player);
        List<ItemStack> list = lootTable.generateLootForPools(ScalingHealth.random, contextBuilder.build());
        list.forEach(stack -> event.getDrops().add(entity.entityDropItem(stack)));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onXPDropped(LivingExperienceDropEvent event) {
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
    private static EntityPlayer getPlayerThatCausedDeath(DamageSource source) {
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
    public static void onPlayerDied(LivingDeathEvent event) {
        if (event.getEntity() == null || !(event.getEntity() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntity();

        if (ModuleAprilTricks.instance.isEnabled() && ModuleAprilTricks.instance.isRightDay()) {
//            ScalingHealth.proxy.playSoundOnClient(player, ModSounds.PLAYER_DIED, 0.6f, 1f);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
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
    public static void onPlayerJoinedServer(PlayerLoggedInEvent event) {
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
    public static void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        /*
        if (!event.getEntityPlayer().world.isRemote && Config.Client.Difficulty.warnWhenSleeping && Config.Difficulty.forSleeping != 0f) {
            ChatHelper.translate(event.getEntityPlayer(), ScalingHealth.i18n.getKey("misc", "sleepWarning"));
        }
        */
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
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
