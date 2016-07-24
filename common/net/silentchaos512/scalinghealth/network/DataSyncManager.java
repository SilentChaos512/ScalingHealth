package net.silentchaos512.scalinghealth.network;

import java.util.Hashtable;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLLog;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;

public class DataSyncManager {

  public static final Hashtable<String, Integer> packetSyncManager = new Hashtable<String, Integer>();

//  public static void requestGuiOpenPacket(EntityPlayer player, int id) {
//
//    if (player.worldObj.isRemote) {
//      NBTTagCompound tag = new NBTTagCompound();
//      tag.setString("username", player.getName());
//      tag.setInteger("guiid", id);
//      ScalingHealth.networkManager.sendToServer(new PacketScalingHealth().setId("guiRequest").setNBT(tag));
//    } else {
//      FMLLog.log(Level.DEBUG, "GuiOpen packet can only be requested from CLIENT side, aborting!",
//          ScalingHealth.nObj());
//    }
//  }

  public static void requestServerToClientMessage(String messageID, EntityPlayerMP client,
      NBTTagCompound message, boolean requirePacket) {

    if (requirePacket) {
      packetSyncManager.put(messageID + "_" + client.getName(), 0);
      ScalingHealth.networkManager.sendTo(new PacketScalingHealth().setId(messageID).setNBT(message), client);
    }
    if (packetSyncManager.containsKey(messageID + "_" + client.getName())) {
      packetSyncManager.put(messageID + "_" + client.getName(),
          packetSyncManager.get(messageID + "_" + client.getName()) + 1);
      if (packetSyncManager.get(messageID + "_" + client.getName()) >= ConfigScalingHealth.PACKET_DELAY) {
        packetSyncManager.put(messageID + "_" + client.getName(), 0);
        ScalingHealth.networkManager.sendTo(new PacketScalingHealth().setId(messageID).setNBT(message), client);
      }
    } else {
      packetSyncManager.put(messageID, 0);
    }
  }
}
