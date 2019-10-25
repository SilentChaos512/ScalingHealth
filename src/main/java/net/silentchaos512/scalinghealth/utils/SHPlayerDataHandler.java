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

package net.silentchaos512.scalinghealth.utils;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.silentchaos512.lib.util.AttributeHelper;
import net.silentchaos512.lib.util.EntityHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.compat.gamestages.SHGameStagesCompat;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.lib.EnumAreaDifficultyMode;
import net.silentchaos512.scalinghealth.network.NetworkHandler;
import net.silentchaos512.scalinghealth.network.message.MessageDataSync;
import net.silentchaos512.scalinghealth.network.message.MessageWorldDataSync;
import net.silentchaos512.scalinghealth.scoreboard.SHScoreCriteria;
import net.silentchaos512.scalinghealth.world.ScalingHealthSavedData;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class SHPlayerDataHandler {
    private static final String NBT_ROOT = ScalingHealth.MOD_ID_LOWER + "_data";

    private static final Map<Integer, PlayerData> playerData = new ConcurrentHashMap<>();

    private SHPlayerDataHandler() {}

    @Nullable
    public static PlayerData get(EntityPlayer player) {
        if (player instanceof FakePlayer && !Config.FakePlayer.haveDifficulty) {
            return null;
        }

        int key = getKey(player);
        if (!playerData.containsKey(key)) {
            playerData.put(key, new PlayerData(player));
        }

        PlayerData data = playerData.get(key);
        if (data != null && data.playerWR.get() != player) {
            NBTTagCompound tags = new NBTTagCompound();
            data.writeToNBT(tags);
            playerData.remove(key);
            data = get(player);
            assert data != null;
            data.readFromNBT(tags);
        }

        return data;
    }

    private static void cleanup() {
        List<Integer> remove = new ArrayList<>();

        for (int i : playerData.keySet()) {
            PlayerData d = playerData.get(i);
            if (d != null && d.playerWR.get() == null) {
                remove.add(i);
            }
        }

        for (int i : remove) {
            playerData.remove(i);
        }
    }

    private static int getKey(EntityPlayer player) {
        return player.hashCode() << 1 + (player.world.isRemote ? 1 : 0);
    }

    private static NBTTagCompound getDataCompoundForPlayer(EntityPlayer player) {
        NBTTagCompound forgeData = player.getEntityData();
        if (!forgeData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            forgeData.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
        }

        NBTTagCompound persistentData = forgeData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        if (!persistentData.hasKey(NBT_ROOT)) {
            persistentData.setTag(NBT_ROOT, new NBTTagCompound());
        }

        return persistentData.getCompoundTag(NBT_ROOT);
    }

    public static class EventHandler {
        @SubscribeEvent
        public void onServerTick(ServerTickEvent event) {
            if (event.phase == Phase.END) {
                SHPlayerDataHandler.cleanup();
            }
        }

        @SubscribeEvent
        public void onPlayerTick(LivingUpdateEvent event) {
            if (event.getEntityLiving() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) event.getEntityLiving();
                PlayerData data = get(player);
                if (data != null) {
                    data.tick();
                }

                // Get data from nearby players.
                if (!player.world.isRemote && player.world.getTotalWorldTime() % 5 * Config.PACKET_DELAY == 0) {
                    for (EntityPlayer p : getNearbyPlayers(player)) {
                        PlayerData data1 = get(p);
                        if (data1 != null) {
                            IMessage message = new MessageDataSync(data1, p);
                            NetworkHandler.INSTANCE.sendTo(message, (EntityPlayerMP) player);
                        }
                    }
                }
            }
        }

        private static Collection<EntityPlayer> getNearbyPlayers(EntityPlayer center) {
            int radius = Config.Difficulty.searchRadius;
            int radiusSquared = radius <= 0 ? Integer.MAX_VALUE : radius * radius;
            BlockPos centerPos = center.getPosition();
            return center.world.getPlayers(EntityPlayer.class, p ->
                    p != null && !p.equals(center) && p.getDistanceSq(centerPos) < radiusSquared);
        }

        @SubscribeEvent
        public void onPlayerLogin(PlayerLoggedInEvent event) {
            if (event.player instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) event.player;

                PlayerData data = get(event.player);
                if (data != null) {
                    IMessage message = new MessageDataSync(data, event.player);
                    NetworkHandler.INSTANCE.sendTo(message, playerMP);
                } else {
                    ScalingHealth.LOGGER.error("Player data for '{}' not found!", event.player.getName());
                }

                if (Config.Difficulty.AREA_DIFFICULTY_MODE == EnumAreaDifficultyMode.SERVER_WIDE) {
                    IMessage message2 = new MessageWorldDataSync(ScalingHealthSavedData.get(event.player.world));
                    NetworkHandler.INSTANCE.sendTo(message2, playerMP);
                }
            }
        }
    }

    public static class PlayerData {
        static final String NBT_DIFFICULTY = "difficulty";
        static final String NBT_HEALTH = "health";
        static final String NBT_MAX_HEALTH = "max_health";
        static final String NBT_LAST_LOGIN = "last_login";

        private static final UUID UUID_XP_HEALTH_BONUS = UUID.fromString("3d3cb1b5-03b0-496a-aaac-60bf63ba139b");

        double difficulty = 0.0D;
        float health = 20;
        float maxHealth = Config.Player.Health.startingHealth;
        @SuppressWarnings("UseOfObsoleteDateTimeApi") // Removed in 1.13
        Calendar lastTimePlayed = Calendar.getInstance();

        WeakReference<EntityPlayer> playerWR;
        private final boolean client;
        private int lastPosX = 0;
        @SuppressWarnings({"FieldCanBeLocal", "unused"}) // Leaving alone for compatibility
        private int lastPosY = 0;
        private int lastPosZ = 0;

        public PlayerData(EntityPlayer player) {
            playerWR = new WeakReference<>(player);
            client = player.world.isRemote;
            load();
        }

        public double getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(double value) {
            EntityPlayer player = playerWR.get();

            // Player exempt from difficulty?
            if (Config.Difficulty.DIFFICULTY_EXEMPT_PLAYERS.contains(player)) {
                difficulty = 0;
            }
            // Non-exempt, just clamp between min and max configs.
            else {
                difficulty = MathHelper.clamp(value, Config.Difficulty.minValue, Config.Difficulty.maxValue);
            }

            // Update scoreboard
            if (player != null) {
                SHScoreCriteria.updateScore(player, (int) difficulty);
            }
        }

        public void incrementDifficulty(double amount) {
            incrementDifficulty(amount, false);
        }

        public void incrementDifficulty(double amount, boolean alsoAffectWorldDifficulty) {
            EntityPlayer player = playerWR.get();
            if (player != null) {
                // Difficulty disabled via game rule?
                if (!player.world.getGameRules().getBoolean(ScalingHealth.GAME_RULE_DIFFICULTY)) {
                    return;
                }
                // Multiplier for this dimension?
                if (Config.Difficulty.DIMENSION_INCREASE_MULTIPLIER.containsKey(player.dimension)) {
                    amount *= Config.Difficulty.DIMENSION_INCREASE_MULTIPLIER.get(player.dimension);
                }
            }

            setDifficulty(difficulty + amount);

            if (alsoAffectWorldDifficulty && player != null) {
                ScalingHealthSavedData data = ScalingHealthSavedData.get(player.world);
                if (data != null) {
                    data.difficulty += amount;
                    data.markDirty();
                }
            }
        }

        public float getHealth() {
            return health;
        }

        public float getMaxHealth() {
            if (maxHealth < 2)
                maxHealth = Config.Player.Health.minHealth;
            return maxHealth;
        }

        public void setMaxHealth(float value) {
            if (!Config.Player.Health.allowModify)
                return;

            int configMax = Config.Player.Health.maxHealth <= 0 ? Integer.MAX_VALUE : Config.Player.Health.maxHealth;

            maxHealth = MathHelper.clamp(value, 2, configMax);

            EntityPlayer player = playerWR.get();
            if (player != null)
                ModifierHandler.setMaxHealth(player, maxHealth, 0);

            save();
            sendUpdateMessage();
        }

        public void incrementMaxHealth(float amount) {
            setMaxHealth(maxHealth + amount);

            EntityPlayer player = playerWR.get();
            if (player != null) {
                int current = (int) player.getHealth();
                EntityHelper.heal(player, amount, Config.Items.Heart.healingEvent);
                int newHealth = (int) player.getHealth();

                if (current + (int) amount != newHealth) {
                    ScalingHealth.logHelper.warn("Another mod seems to have canceled healing from a max health increase (player {})", player.getName());
                }
            }
        }

        @SuppressWarnings("UseOfObsoleteDateTimeApi")
        public Calendar getLastTimePlayed() {
            //noinspection AssignmentOrReturnOfFieldWithMutableType
            return lastTimePlayed;
        }

        private void tick() {
            if (!client) {
                EntityPlayer player = playerWR.get();
                if (player == null)
                    return;

                // Increase player difficulty.
                if (player.world.getTotalWorldTime() % 20 == 0) {
                    float amount = Config.Difficulty.perSecond;

                    // Idle multiplier
                    if (lastPosX == (int) player.posX && lastPosZ == (int) player.posZ)
                        amount *= Config.Difficulty.idleMulti;

                    // TODO: Multiplier for other dimensions?

                    incrementDifficulty(amount, false);

                    lastPosX = (int) player.posX;
                    lastPosY = (int) player.posY;
                    lastPosZ = (int) player.posZ;

                    if (!Config.Difficulty.DIFFICULTY_BY_GAME_STAGES.isEmpty() && Loader.isModLoaded("gamestages")) {
                        setDifficulty(SHGameStagesCompat.getDifficultyFromStages(player));
                    }

                    // Health by XP?
                    if (!Config.Player.Health.byXP.isEmpty()) {
                        int highestLevel = 0;
                        for (int key : Config.Player.Health.byXP.keySet())
                            if (key > highestLevel && key <= player.experienceLevel)
                                highestLevel = key;

                        if (Config.Player.Health.byXP.containsKey(highestLevel)) {
                            float modAmount = Config.Player.Health.byXP.get(highestLevel) - Config.Player.Health.startingHealth;
                            AttributeHelper.apply(player, SharedMonsterAttributes.MAX_HEALTH, UUID_XP_HEALTH_BONUS, "health_from_xp", modAmount, 0);
                        } else {
                            AttributeHelper.remove(player, SharedMonsterAttributes.MAX_HEALTH, UUID_XP_HEALTH_BONUS);
                        }
                    }
                }
                health = player.getHealth();
                // Sync with client?
                if (player.world.getTotalWorldTime() % Config.PACKET_DELAY == 0) {
                    save();
                    sendUpdateMessage();
                }
            }
        }

        private void sendUpdateMessage() {
            if (!client) {
                EntityPlayer player = playerWR.get();

                if (player != null) {
                    EntityPlayerMP playerMP = (EntityPlayerMP) player;
                    PlayerData data = get(player);

                    if (data != null) {
                        IMessage message = new MessageDataSync(data, player);
                        NetworkHandler.INSTANCE.sendTo(message, playerMP);

                        if (Config.Difficulty.AREA_DIFFICULTY_MODE == EnumAreaDifficultyMode.SERVER_WIDE) {
                            IMessage message2 = new MessageWorldDataSync(ScalingHealthSavedData.get(player.world));
                            NetworkHandler.INSTANCE.sendTo(message2, playerMP);
                        }
                    }
                }
            }
        }

        public void save() {
            if (!client) {
                EntityPlayer player = playerWR.get();
                if (player != null) {
                    NBTTagCompound tags = getDataCompoundForPlayer(player);
                    writeToNBT(tags);
                }
            }
        }

        public void writeToNBT(NBTTagCompound tags) {
            tags.setDouble(NBT_DIFFICULTY, difficulty);
            tags.setFloat(NBT_HEALTH, health);
            tags.setFloat(NBT_MAX_HEALTH, maxHealth);

            int year = lastTimePlayed.get(Calendar.YEAR);
            int month = lastTimePlayed.get(Calendar.MONTH) + 1;
            int date = lastTimePlayed.get(Calendar.DATE);
            String dateString = year + "/" + month + "/" + date;
            tags.setString(NBT_LAST_LOGIN, dateString);
        }

        public void load() {
            if (!client) {
                EntityPlayer player = playerWR.get();
                if (player != null) {
                    NBTTagCompound tags = getDataCompoundForPlayer(player);
                    readFromNBT(tags);
                }
            }
        }

        public void readFromNBT(NBTTagCompound tags) {
            difficulty = tags.getDouble(NBT_DIFFICULTY);
            health = tags.getFloat(NBT_HEALTH);
            maxHealth = tags.getFloat(NBT_MAX_HEALTH);

            String lastDatePlayed = tags.getString(NBT_LAST_LOGIN);
            String[] dateParts = lastDatePlayed.split("/");
            if (dateParts.length >= 3) {
                try {
                    int year = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]) - 1;
                    int date = Integer.parseInt(dateParts[2]);
                    //noinspection MagicConstant
                    lastTimePlayed.set(year, month, date);
                } catch (NumberFormatException ex) {
                    ScalingHealth.logHelper.warn("Could not parse player's last login time.");
                    ex.printStackTrace();
                }
            }
        }
    }
}
