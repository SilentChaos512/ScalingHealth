package net.silentchaos512.scalinghealth.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.network.NetworkHandler;
import net.silentchaos512.scalinghealth.network.message.MessageDataSync;

public class SHPlayerDataHandler {

  public static final String NBT_ROOT = ScalingHealth.MOD_ID_LOWER + "_data";

  private static Map<Integer, PlayerData> playerData = new HashMap();

  public static PlayerData get(EntityPlayer player) {

    int key = getKey(player);
    if (!playerData.containsKey(key)) {
      playerData.put(key, new PlayerData(player));
    }

    PlayerData data = playerData.get(key);
    if (data.playerWR.get() != player) {
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

    return player.hashCode() << 1 + (player.worldObj.isRemote ? 1 : 0);
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
        if (!player.worldObj.isRemote
            && player.worldObj.getTotalWorldTime() % 5 * ConfigScalingHealth.PACKET_DELAY == 0) {
          int radius = ConfigScalingHealth.DIFFICULTY_SEARCH_RADIUS;
          int radiusSquared = radius <= 0 ? Integer.MAX_VALUE : radius * radius;
          for (EntityPlayer p : player.worldObj.getPlayers(EntityPlayer.class,
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
        MessageDataSync message = new MessageDataSync(get(event.player), event.player);
        NetworkHandler.INSTANCE.sendTo(message, (EntityPlayerMP) event.player);
      }
    }
  }

  public static class PlayerData {

    public static final String NBT_DIFFICULTY = "difficulty";
    public static final String NBT_HEALTH = "health";
    public static final String NBT_MAX_HEALTH = "max_health";

    double difficulty = 0.0D;
    float health = 20;
    float maxHealth = ConfigScalingHealth.PLAYER_STARTING_HEALTH;

    public WeakReference<EntityPlayer> playerWR;
    private final boolean client;
    private int lastPosX = 0;
    private int lastPosY = 0;
    private int lastPosZ = 0;

    public PlayerData(EntityPlayer player) {

      playerWR = new WeakReference<EntityPlayer>(player);
      client = player.worldObj.isRemote;

      load();
    }

    public double getDifficulty() {

      return difficulty;
    }

    public void setDifficulty(double value) {

      difficulty = MathHelper.clamp_double(value, 0, ConfigScalingHealth.DIFFICULTY_MAX);
    }

    public void incrementDifficulty(double amount) {

      setDifficulty(difficulty + amount);
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

      maxHealth = MathHelper.clamp_float(value, 2, configMax);
      ScalingHealth.logHelper.debug(value, maxHealth);

      EntityPlayer player = playerWR.get();
      if (player != null)
        ModifierHandler.setMaxHealth(playerWR.get(), maxHealth, 0);

      save();
      sendUpdateMessage();
    }

    public void incrementMaxHealth(float amount) {

      setMaxHealth(maxHealth + amount);

      EntityPlayer player = playerWR.get();
      if (player != null)
        player.heal(amount);
    }

    private void tick() {

      if (!client) {
        EntityPlayer player = playerWR.get();
        if (player == null)
          return;

        // Increase player difficulty.
        if (player.worldObj.getTotalWorldTime() % 20 == 0) {
          float amount = ConfigScalingHealth.DIFFICULTY_PER_SECOND;

          // Idle multiplier
          if (lastPosX == (int) player.posX && lastPosZ == (int) player.posZ)
            amount *= ConfigScalingHealth.DIFFICULTY_IDLE_MULTI;

          // TODO: Multiplier for other dimensions?

          incrementDifficulty(amount);

          lastPosX = (int) player.posX;
          lastPosY = (int) player.posY;
          lastPosZ = (int) player.posZ;
        }
        // Sync with client?
        if (player.worldObj.getTotalWorldTime() % ConfigScalingHealth.PACKET_DELAY == 0) {
          health = player.getHealth();
          save();
          sendUpdateMessage();
        }
      }
    }

    private void sendUpdateMessage() {

      if (!client) {
        EntityPlayer player = playerWR.get();
        MessageDataSync message = new MessageDataSync(get(player), player);
        NetworkHandler.INSTANCE.sendTo(message, (EntityPlayerMP) player);
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
    }
  }
}
