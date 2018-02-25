package net.silentchaos512.scalinghealth.network.message;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.client.ClientTickHandler;
import net.silentchaos512.scalinghealth.network.Message;
import net.silentchaos512.scalinghealth.world.ScalingHealthSavedData;

public class MessageWorldDataSync extends Message {

  public NBTTagCompound tags;

  public MessageWorldDataSync() {

  }

  public MessageWorldDataSync(ScalingHealthSavedData data) {

    tags = new NBTTagCompound();
    data.writeToNBT(tags);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IMessage handleMessage(MessageContext context) {

    ClientTickHandler.scheduledActions.add(() -> {
      EntityPlayer clientPlayer = ScalingHealth.proxy.getClientPlayer();
      if (clientPlayer != null) {
        ScalingHealthSavedData data = ScalingHealthSavedData.get(clientPlayer.world);
        if (data != null) {
          data.readFromNBT(tags);
        }
      }
    });

    return null;
  }
}
