package net.silentchaos512.scalinghealth.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.utils.ScalingHealthSaveStorage;

public class PacketScalingHealth implements IMessage {

  public String id = null;
  public NBTTagCompound tag = null;

  public PacketScalingHealth() {

  }

  public PacketScalingHealth setNBT(NBTTagCompound tag) {

    this.tag = tag;
    return this;
  }

  public PacketScalingHealth setId(String id) {

    this.id = id;
    return this;
  }

  @Override
  public void fromBytes(ByteBuf buf) {

    NBTTagCompound byteReadTag = ByteBufUtils.readTag(buf);
    this.id = byteReadTag.getString("id");
    this.tag = byteReadTag.getCompoundTag("tag");
  }

  @Override
  public void toBytes(ByteBuf buf) {

    NBTTagCompound byteWriteTag = new NBTTagCompound();
    if (id != null)
      byteWriteTag.setString("id", id);
    if (tag != null)
      byteWriteTag.setTag("tag", tag);
    ByteBufUtils.writeTag(buf, byteWriteTag);
  }

  public static class Handler implements IMessageHandler<PacketScalingHealth, IMessage> {

    @Override
    public IMessage onMessage(PacketScalingHealth message, MessageContext ctx) {

      if (message.id.equalsIgnoreCase("worldData")) {
        ScalingHealthSaveStorage.commonTag = message.tag;
      }
      if (message.id.equalsIgnoreCase("playerData")) {
        String tagName = message.tag.getString("username");
        EntityPlayer clientPlayer = ScalingHealth.proxy.getClientPlayer();
        String username = clientPlayer == null ? "" : clientPlayer.getName();
        if (username.equalsIgnoreCase(tagName))
          ScalingHealthSaveStorage.clientPlayerData = message.tag;
        else
          ScalingHealthSaveStorage.playerData.put(tagName, message.tag);
      }
      return null;
    }
  }
}
