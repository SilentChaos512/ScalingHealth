package net.silentchaos512.scalinghealth.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.UUID;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.silentchaos512.lib.util.LogHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.network.DataSyncManager;

public class ScalingHealthSaveStorage {

  public static final UUID MODIFIER_ID = UUID.fromString("af286358-cd54-11e4-afdc-1681e6b88ec1");

  public static final String NBT_WORLD_DIFFICULTY = "difficulty";
  public static final String NBT_PLAYER_HEALTH = "health";

  public static NBTTagCompound commonTag;
  public static Hashtable<String, NBTTagCompound> playerData = new Hashtable();

  public static NBTTagCompound clientPlayerData;

  public static double getDifficulty(World world) {

    checkCommonTag();
    return commonTag.getDouble(NBT_WORLD_DIFFICULTY);
  }

  private static void setDifficulty(World world, double value) {

    checkCommonTag();
    commonTag.setDouble(NBT_WORLD_DIFFICULTY, value);
    sendUpdatePacketToAllPlayers(world);
  }

  public static void incrementDifficulty(World world, double amount) {

    setDifficulty(world,
        Math.min(getDifficulty(world) + amount, ConfigScalingHealth.DIFFICULTY_MAX));
  }

  public static int getPlayerHealth(EntityPlayer player) {

    return getPlayerTag(player).getInteger(NBT_PLAYER_HEALTH);
  }

  public static void setPlayerHealth(EntityPlayer player, int value) {

    getPlayerTag(player).setInteger(NBT_PLAYER_HEALTH, value);

    AttributeModifier modifier = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
        .getModifier(MODIFIER_ID);
    if (modifier != null) {
      AttributeModifier copy = new AttributeModifier(modifier.getID(), modifier.getName(),
          value - 20, modifier.getOperation());
      player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(modifier);
      player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(copy);
      // player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
      // .applyModifier(new AttributeModifier(MODIFIER_ID, "ScalingHealth.PlayerHealthDfifference",
      // -(player.getMaxHealth() - value), 0));
    }

    sendUpdatePacketToPlayer((EntityPlayerMP) player);
  }

  public static void incrementPlayerHealth(EntityPlayer player) {

    if (player instanceof EntityPlayerMP) {
      int currentHealth = getPlayerHealth(player);

      // Don't increase beyond maximum, if a limit has been set (non-positive max means unlimited)
      if (ConfigScalingHealth.PLAYER_HEALTH_MAX > 0
          && currentHealth + 2 > ConfigScalingHealth.PLAYER_HEALTH_MAX)
        return;

      setPlayerHealth(player, currentHealth + 2);
      player.heal(2.0f);
    }

    ScalingHealth.logHelper.debug(getPlayerHealth(player));
  }

  public static void resetPlayerHealth(EntityPlayer player) {

    ScalingHealth.logHelper.info("Resetting health of " + player.getName() + " to "
        + ConfigScalingHealth.PLAYER_STARTING_HEALTH + ".");
    setPlayerHealth(player, ConfigScalingHealth.PLAYER_STARTING_HEALTH);
  }

  private static NBTTagCompound getPlayerTag(EntityPlayer player) {

    NBTTagCompound tag = playerData.get(player.getName());

    if (tag == null || tag.hasNoTags()) {
      tag = new NBTTagCompound();
      tag.setInteger(NBT_PLAYER_HEALTH, ConfigScalingHealth.PLAYER_STARTING_HEALTH);
      playerData.put(player.getName(), tag);
    }

    return tag;
  }

  private static void checkCommonTag() {

    if (commonTag == null || commonTag.hasNoTags()) {
      commonTag = new NBTTagCompound();
      commonTag.setDouble(NBT_WORLD_DIFFICULTY, ConfigScalingHealth.DIFFICULTY_DEFAULT);
    }
  }

  public static void sendUpdatePacketToPlayer(EntityPlayerMP player) {

    DataSyncManager.requestServerToClientMessage("worldData", player, commonTag, false);
  }

  public static void sendUpdatePacketToAllPlayers(World world) {

    if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
      for (EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayerList()) {
        if (player != null) {
          sendUpdatePacketToPlayer(player);
        }
      }
    }
  }

  public static boolean loadPlayerFile(PlayerEvent.LoadFromFile event) {

    LogHelper log = ScalingHealth.logHelper;

    try {
      EntityPlayer player = event.getEntityPlayer();
      if (player != null && !player.worldObj.isRemote && playerData.get(player.getName()) != null) {
        log.info("Begin loading file for player " + player.getName() + ".");
        File f = event.getPlayerDirectory();
        if (f != null) {
          String fPath = f.getAbsolutePath();
          File worldSaveFile = new File(
              fPath + "/" + ScalingHealth.MOD_ID + "Data_" + player.getName() + ".dat");
          if (!worldSaveFile.exists()) {
            log.info("Player file not found. Creating it now.");
            worldSaveFile.createNewFile();
          }

          try (FileInputStream input = new FileInputStream(worldSaveFile)) {
            playerData.put(player.getName(), CompressedStreamTools.readCompressed(input));
          } catch (IOException ex) {
            log.warning(
                "Unable to read NBT from player file. Ignore if this is your first launch.");
          }

          return true;
        }

        return false;
      }

      return false;
    } catch (Exception ex) {
      log.severe("Caught an exception while loading player file!\n" + ex);
      return false;
    }
  }

  public static boolean savePlayerFile(PlayerEvent.SaveToFile event) {

    LogHelper log = ScalingHealth.logHelper;

    try {
      EntityPlayer player = event.getEntityPlayer();
      if (player != null && !player.worldObj.isRemote && playerData.get(player.getName()) != null) {
        log.info("Begin saving file for player " + player.getName() + ".");
        File f = event.getPlayerDirectory();
        if (f != null) {
          String fPath = f.getAbsolutePath();
          File worldSaveFile = new File(
              fPath + "/" + ScalingHealth.MOD_ID + "Data_" + player.getName() + ".dat");
          if (!worldSaveFile.exists()) {
            worldSaveFile.createNewFile();
          }

          try (FileOutputStream output = new FileOutputStream(worldSaveFile)) {
            CompressedStreamTools.writeCompressed(playerData.get(player.getName()), output);
          }

          return true;
        }

        return false;
      }

      return false;
    } catch (Exception ex) {
      log.severe("Caught an exception while saving player file!\n" + ex);
      return false;
    }
  }

  public static boolean loadServerWorldFile(WorldEvent.Load event) {

    LogHelper log = ScalingHealth.logHelper;
    try {
      World w = event.getWorld();
      if (w != null && !w.isRemote && w.provider != null && w.provider.getDimension() == 0) {
        log.info("Begin loading world file.");
        File f = event.getWorld().getSaveHandler().getWorldDirectory();
        if (f != null) {
          String fPath = f.getAbsolutePath();
          File worldSaveFile = new File(fPath + "/ScalingHealthData.dat");
          if (!worldSaveFile.exists()) {
            log.info("World file not found. Creating it now.");
            worldSaveFile.createNewFile();
          }

          try (FileInputStream iStream = new FileInputStream(worldSaveFile)) {
            commonTag = CompressedStreamTools.readCompressed(iStream);
          } catch (IOException e) {
            log.info("Unable to read from world file. Ignore if this is your first launch.");
            commonTag = new NBTTagCompound();
          }
          return true;
        }
        return false;
      }
      return false;
    } catch (Exception ex) {
      log.severe("Caught an exception while loading world file!\n" + ex);
      return false;
    }
  }

  public static boolean saveServerWorldFile(WorldEvent.Save event) {

    LogHelper log = ScalingHealth.logHelper;

    try {
      World w = event.getWorld();
      if (w != null && !w.isRemote && w.provider != null && w.provider.getDimension() == 0) {
        log.info("Begin saving world file.");
        File f = event.getWorld().getSaveHandler().getWorldDirectory();
        if (f != null) {
          String fPath = f.getAbsolutePath();
          File worldSaveFile = new File(fPath + "/ScalingHealthData.dat");
          if (!worldSaveFile.exists()) {
            throw new IOException("File does not exist!");
          }

          try (FileOutputStream oStream = new FileOutputStream(worldSaveFile)) {
            CompressedStreamTools.writeCompressed(commonTag, oStream);
          } catch (IOException e) {
            throw new IOException(
                "Unable to write NBT to server save file! Please, delete the ScalingHealthData.dat in your save folder and restart the server!");
          } catch (NullPointerException e) {
            throw new IOException("Server NBT does not exists!!" + e.getMessage());
          }
          return true;
        }
        return false;
      }
      return false;
    } catch (Exception ex) {
      log.severe("Caught an exception while saving world file!\n" + ex);
      return false;
    }
  }
}
