package net.silentchaos512.scalinghealth.network.message;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.client.ClientTickHandler;
import net.silentchaos512.scalinghealth.network.Message;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public class MessageDataSync extends Message {

  public NBTTagCompound tags;

  public MessageDataSync() {

  }

  public MessageDataSync(PlayerData data) {

    tags = new NBTTagCompound();
    data.writeToNBT(tags);
    ;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IMessage handleMessage(MessageContext context) {

    ClientTickHandler.scheduledActions.add(() -> {
      PlayerData data = SHPlayerDataHandler.get(ScalingHealth.proxy.getClientPlayer());
      data.readFromNBT(tags);
    });

    return null;
  }
}
