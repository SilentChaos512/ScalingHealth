package net.silentchaos512.scalinghealth.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.silentchaos512.lib.event.ClientTicks;
import net.silentchaos512.scalinghealth.event.BlightHandler;
import net.silentchaos512.scalinghealth.network.Message;

import javax.annotation.Nullable;


public class MessageMarkBlight extends Message {
    @SuppressWarnings("WeakerAccess")
    public int entityId;

    @SuppressWarnings("unused")
    public MessageMarkBlight() { }

    public MessageMarkBlight(EntityLivingBase entity) {
        entityId = entity.getEntityId();
    }

    @Override
    @Nullable
    @SideOnly(Side.CLIENT)
    public IMessage handleMessage(MessageContext context) {
        ClientTicks.scheduleAction(() -> {
            Entity entity = Minecraft.getMinecraft().world.getEntityByID(entityId);
            if (entity instanceof EntityLivingBase)
                BlightHandler.markBlight((EntityLivingBase) entity);
        });

        return null;
    }
}
