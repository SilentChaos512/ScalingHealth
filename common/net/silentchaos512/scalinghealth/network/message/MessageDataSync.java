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
import net.silentchaos512.scalinghealth.utils.ModifierHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public class MessageDataSync extends Message {

  public NBTTagCompound tags;
  public String playerName;

  public MessageDataSync() {

  }

  public MessageDataSync(PlayerData data, EntityPlayer player) {

    tags = new NBTTagCompound();
    data.writeToNBT(tags);
    this.playerName = player.getName();
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IMessage handleMessage(MessageContext context) {

    ClientTickHandler.scheduledActions.add(() -> {
      EntityPlayer player = ScalingHealth.proxy.getClientPlayer().worldObj
          .getPlayerEntityByName(playerName);
      if (player != null) {
        PlayerData data = SHPlayerDataHandler.get(player);
        data.readFromNBT(tags);
        // Set players health and max health.
        ModifierHandler.setMaxHealth(player, data.getMaxHealth(), 0);
        if (data.getHealth() > 0f)
          player.setHealth(data.getHealth());
      }
    });

    return null;
  }
}
