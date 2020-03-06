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
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
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
import net.silentchaos512.scalinghealth.capability.PlayerDataCapability;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.DimensionConfig;
import net.silentchaos512.scalinghealth.config.EvalVars;
import net.silentchaos512.scalinghealth.init.ModSounds;
import net.silentchaos512.scalinghealth.lib.EntityGroup;
import net.silentchaos512.scalinghealth.lib.module.ModuleAprilTricks;
import net.silentchaos512.scalinghealth.network.ClientLoginMessage;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.utils.SHDifficulty;
import net.silentchaos512.scalinghealth.utils.SHMobs;
import net.silentchaos512.scalinghealth.utils.SHPlayers;
import net.silentchaos512.utils.MathUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public final class ScalingHealthCommonEvents {
    private ScalingHealthCommonEvents() {}

    public static List<UUID> spawnerSpawns = new ArrayList<>();

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        World world = player.world;
        ScalingHealth.LOGGER.info("Sending login packet to player {}", player);
        ClientLoginMessage msg = new ClientLoginMessage(SHDifficulty.areaMode(world), (float) SHDifficulty.maxValue(world));
        Network.channel.sendTo(msg, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event){
        if(!(event.getPlayer() instanceof ServerPlayerEntity)) return;
        DimensionType type = event.getTo();
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        ScalingHealth.LOGGER.info("Sending login packet to player {}, resulting of a dim change", player);
        ClientLoginMessage msg = new ClientLoginMessage(Config.get(type).difficulty.areaMode.get(), Config.get(type).difficulty.maxValue.get().floatValue());
        Network.channel.sendTo(msg, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    @SubscribeEvent
    public static void onSpawn(LivingSpawnEvent.CheckSpawn event){
        if(!(event.getEntityLiving() instanceof MobEntity)) return;
        if(event.getSpawnReason() == SpawnReason.SPAWNER) spawnerSpawns.add(event.getEntityLiving().getUniqueID());
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;

        LivingEntity entity = (LivingEntity) event.getEntity();
        World world = entity.world;
        if (world.isRemote) return;
        MinecraftServer server = world.getServer();
        if (server == null) return;

        // Mob loot disabled?
        if (!world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) return;

        PlayerEntity player = getPlayerThatCausedDeath(event.getSource());

        // Get the bonus drops loot table for this mob type
        Optional<ResourceLocation> tableName = EntityGroup.from(entity, true).getBonusDropsLootTable();
        if (!tableName.isPresent()) return;

        LootTable lootTable = server.getLootTableManager().getLootTableFromLocation(tableName.get());
        LootContext.Builder contextBuilder = new LootContext.Builder((ServerWorld) world)
                .withParameter(LootParameters.THIS_ENTITY, entity)
                .withParameter(LootParameters.POSITION, entity.getPosition())
                .withParameter(LootParameters.DAMAGE_SOURCE, event.getSource())
                .withNullableParameter(LootParameters.KILLER_ENTITY, player)
                .withNullableParameter(LootParameters.DIRECT_KILLER_ENTITY, player)
                .withNullableParameter(LootParameters.LAST_DAMAGE_PLAYER, player);
        if (player != null) contextBuilder.withLuck(player.getLuck());
        List<ItemStack> list = lootTable.generate(contextBuilder.build(LootParameterSets.ENTITY));
        list.forEach(stack -> event.getDrops().add(dropItem(entity, world, stack)));
    }

    private static ItemEntity dropItem(LivingEntity entity, World world, ItemStack stack) {
        return new ItemEntity(world, entity.posX, entity.posY + entity.getHeight() / 2, entity.posZ, stack);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMobXPDropped(LivingExperienceDropEvent event) {
        LivingEntity entity = event.getEntityLiving();
        // Additional XP from all mobs.
        short difficulty = (short) SHDifficulty.areaDifficulty(entity.world, entity.getPosition());
        float multi = (float) (1.0f + SHMobs.xpBoost(entity.world) * difficulty);

        float amount = event.getDroppedExperience();
        amount *= multi;

        // Additional XP from blights.
        if(entity instanceof MobEntity) {
            if (SHMobs.isBlight((MobEntity) entity)) {
                amount *= SHMobs.xpBlightBoost(entity.world);
            }
        }
        event.setDroppedExperience(Math.round(amount));
    }

    @SubscribeEvent
    public static void onLevelChange(PlayerXpEvent.LevelChange event){
        PlayerEntity player = event.getPlayer();
        if(!SHPlayers.xpModeEnabled(player.world)) return;

        double xp = event.getLevels();
        int numberOfIncrease = (int) Math.floor(xp / SHPlayers.levelsPerHp(player.world));

        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(data -> data.addHearts(player, numberOfIncrease * SHPlayers.hpPerLevel(player.world)));
    }

    /**
     * Get the player that caused a mob's death. Could be a FakePlayer or null.
     *
     * @return The player that caused the damage, or the owner of the tamed animal that caused the
     * damage.
     */
    @Nullable
    private static PlayerEntity getPlayerThatCausedDeath(DamageSource source) {
        if (source == null) {
            return null;
        }

        // Player is true source.
        Entity entitySource = source.getTrueSource();
        if (entitySource instanceof PlayerEntity) {
            return (PlayerEntity) entitySource;
        }

        // Player's pet is true source.
        boolean isTamedAnimal = entitySource instanceof TameableEntity
                && ((TameableEntity) entitySource).isTamed();
        if (isTamedAnimal) {
            TameableEntity tamed = (TameableEntity) entitySource;
            if (tamed.getOwner() instanceof PlayerEntity) {
                return (PlayerEntity) tamed.getOwner();
            }
        }
        // No player responsible.
        return null;
    }

    @SubscribeEvent
    public static void onPlayerDied(LivingDeathEvent event) {
        if (event.getEntity() == null || !(event.getEntity() instanceof PlayerEntity)) {
            return;
        }

        PlayerEntity player = (PlayerEntity) event.getEntity();
        ModSounds.PLAYER_DIED.play(player);

        if (ModuleAprilTricks.instance.isEnabled() && ModuleAprilTricks.instance.isRightDay()) {
            ModSounds.PLAYER_DIED.play(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        PlayerEntity player = event.getPlayer();
        if (!player.world.isRemote && Config.CLIENT.warnWhenSleeping.get()) {
            DimensionConfig config = Config.get(player);
            double newDifficulty = EvalVars.apply(config, player, config.difficulty.onPlayerSleep.get());

            if (!MathUtils.doublesEqual(SHDifficulty.getDifficultyOf(player), newDifficulty, 0.1)) {
                ScalingHealth.LOGGER.debug("old={}, new={}", SHDifficulty.getDifficultyOf(player), newDifficulty);
                // Difficulty would change (doesn't change until onPlayerWakeUp)
                String configMsg = SHDifficulty.sleepWarningMessage(player.world);
                ITextComponent text = configMsg.isEmpty()
                        ? new TranslationTextComponent("misc.scalinghealth.sleepWarning")
                        : new StringTextComponent(configMsg);
                player.sendMessage(text);
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
