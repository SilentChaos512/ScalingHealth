package net.silentchaos512.scalinghealth.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.client.ClientTickHandler;
import net.silentchaos512.scalinghealth.event.BlightHandler;
import net.silentchaos512.scalinghealth.network.Message;


public class MessageMarkBlight extends Message {

  public int entityId;

  public MessageMarkBlight() {

  }

  public MessageMarkBlight(EntityLivingBase entity) {

    entityId = entity.getEntityId();
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IMessage handleMessage(MessageContext context) {

    ClientTickHandler.scheduledActions.add(() -> {
      Entity entity = Minecraft.getMinecraft().world.getEntityByID(entityId);
      //ScalingHealth.logHelper.debug("Attempt mark blight... Entity " + entityId + " " + entity);
      if (entity instanceof EntityLivingBase)
        BlightHandler.markBlight((EntityLivingBase) entity);
    });

    return null;
  }
}
