package net.silentchaos512.scalinghealth.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.lib.EnumAreaDifficultyMode;
import net.silentchaos512.scalinghealth.network.NetworkHandler;
import net.silentchaos512.scalinghealth.network.message.MessageDataSync;
import net.silentchaos512.scalinghealth.network.message.MessageWorldDataSync;
import net.silentchaos512.scalinghealth.scoreboard.SHScoreCriteria;
import net.silentchaos512.scalinghealth.world.ScalingHealthSavedData;

public class SHPlayerDataHandler {

  public static final String NBT_ROOT = ScalingHealth.MOD_ID_LOWER + "_data";

  private static Map<Integer, PlayerData> playerData = new HashMap();

  public static @Nullable PlayerData get(EntityPlayer player) {

    if (player instanceof FakePlayer && !ConfigScalingHealth.FAKE_PLAYERS_HAVE_DIFFICULTY) {
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
      data.readFromNBT(tags);
    }

    return data;
  }

  public static void cleanup() {

    List<Integer> remove = new ArrayList();

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

  public static NBTTagCompound getDataCompoundForPlayer(EntityPlayer player) {

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
        SHPlayerDataHandler.get(player).tick();

        // Get data from nearby players.
        if (!player.world.isRemote
            && player.world.getTotalWorldTime() % 5 * ConfigScalingHealth.PACKET_DELAY == 0) {
          int radius = ConfigScalingHealth.DIFFICULTY_SEARCH_RADIUS;
          int radiusSquared = radius <= 0 ? Integer.MAX_VALUE : radius * radius;
          for (EntityPlayer p : player.world.getPlayers(EntityPlayer.class,
              p -> !p.equals(player) && p.getDistanceSq(player.getPosition()) < radiusSquared)) {
            MessageDataSync message = new MessageDataSync(get(p), p);
            NetworkHandler.INSTANCE.sendTo(message, (EntityPlayerMP) player);
          }
        }
      }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {

      if (event.player instanceof EntityPlayerMP) {
        EntityPlayerMP playerMP = (EntityPlayerMP) event.player;

        MessageDataSync message = new MessageDataSync(get(event.player), event.player);
        NetworkHandler.INSTANCE.sendTo(message, playerMP);

        if (ConfigScalingHealth.AREA_DIFFICULTY_MODE == EnumAreaDifficultyMode.SERVER_WIDE) {
          MessageWorldDataSync message2 = new MessageWorldDataSync(
              ScalingHealthSavedData.get(event.player.world));
          NetworkHandler.INSTANCE.sendTo(message2, playerMP);
        }
      }
    }
  }

  public static class PlayerData {

    public static final String NBT_DIFFICULTY = "difficulty";
    public static final String NBT_HEALTH = "health";
    public static final String NBT_MAX_HEALTH = "max_health";
    public static final String NBT_LAST_LOGIN = "last_login";

    double difficulty = 0.0D;
    float health = 20;
    float maxHealth = ConfigScalingHealth.PLAYER_STARTING_HEALTH;
    Calendar lastTimePlayed = Calendar.getInstance();

    public WeakReference<EntityPlayer> playerWR;
    private final boolean client;
    private int lastPosX = 0;
    private int lastPosY = 0;
    private int lastPosZ = 0;

    public PlayerData(EntityPlayer player) {

      playerWR = new WeakReference<EntityPlayer>(player);
      client = player.world.isRemote;

      load();
    }

    public double getDifficulty() {

      return difficulty;
    }

    public void setDifficulty(double value) {

      EntityPlayer player = playerWR.get();

      // Player exempt from difficulty?
      if (ConfigScalingHealth.DIFFICULTY_EXEMPT_PLAYERS.contains(player)) {
        difficulty = 0;
      }
      // Non-exempt, just clamp between min and max configs.
      else {
        difficulty = MathHelper.clamp(value, ConfigScalingHealth.DIFFICULTY_MIN,
            ConfigScalingHealth.DIFFICULTY_MAX);
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
        if (ConfigScalingHealth.DIFFICULTY_DIMENSION_MULTIPLIER.containsKey(player.dimension)) {
          amount *= ConfigScalingHealth.DIFFICULTY_DIMENSION_MULTIPLIER.get(player.dimension);
        }
      }

      setDifficulty(difficulty + amount);

      if (alsoAffectWorldDifficulty) {
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
        maxHealth = ConfigScalingHealth.PLAYER_STARTING_HEALTH;
      return maxHealth;
    }

    public void setMaxHealth(float value) {

      int configMax = ConfigScalingHealth.PLAYER_HEALTH_MAX;
      configMax = configMax <= 0 ? Integer.MAX_VALUE : configMax;

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
        player.setHealth(player.getHealth() + amount);
      }
    }

    public Calendar getLastTimePlayed() {

      return lastTimePlayed;
    }

    private void tick() {

      if (!client) {
        EntityPlayer player = playerWR.get();
        if (player == null)
          return;

        // Increase player difficulty.
        if (player.world.getTotalWorldTime() % 20 == 0) {
          float amount = ConfigScalingHealth.DIFFICULTY_PER_SECOND;

          // Idle multiplier
          if (lastPosX == (int) player.posX && lastPosZ == (int) player.posZ)
            amount *= ConfigScalingHealth.DIFFICULTY_IDLE_MULTI;

          // TODO: Multiplier for other dimensions?

          incrementDifficulty(amount, false);

          lastPosX = (int) player.posX;
          lastPosY = (int) player.posY;
          lastPosZ = (int) player.posZ;
        }
        // Sync with client?
        if (player.world.getTotalWorldTime() % ConfigScalingHealth.PACKET_DELAY == 0) {
          health = player.getHealth();
          save();
          sendUpdateMessage();
        }
      }
    }

    private void sendUpdateMessage() {

      if (!client) {
        EntityPlayer player = playerWR.get();
        EntityPlayerMP playerMP = (EntityPlayerMP) player;

        MessageDataSync message = new MessageDataSync(get(player), player);
        NetworkHandler.INSTANCE.sendTo(message, playerMP);

        if (ConfigScalingHealth.AREA_DIFFICULTY_MODE == EnumAreaDifficultyMode.SERVER_WIDE) {
          MessageWorldDataSync message2 = new MessageWorldDataSync(
              ScalingHealthSavedData.get(player.world));
          NetworkHandler.INSTANCE.sendTo(message2, playerMP);
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
          lastTimePlayed.set(year, month, date);
        } catch (NumberFormatException ex) {
          ScalingHealth.logHelper.warning("Could not parse player's last login time.");
          ex.printStackTrace();
        }
      }
    }
  }
}
