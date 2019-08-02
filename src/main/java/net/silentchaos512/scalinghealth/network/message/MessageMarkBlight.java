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
    @SuppressWarnings("WeakerAccess")
    public boolean isBlight;

    @SuppressWarnings("unused")
    public MessageMarkBlight() { }

    @Deprecated
    public MessageMarkBlight(EntityLivingBase entity) {
        this(entity, true);
    }

    public MessageMarkBlight(EntityLivingBase entity, boolean isBlight) {
        this.entityId = entity.getEntityId();
        this.isBlight = isBlight;
    }

    @Override
    @Nullable
    @SideOnly(Side.CLIENT)
    public IMessage handleMessage(MessageContext context) {
        //noinspection OverlyLongLambda
        ClientTicks.scheduleAction(() -> {
            Minecraft mc = Minecraft.getMinecraft();
            // Sometimes MC client instance is null, seems to happen when connecting to servers
            //noinspection ConstantConditions -- mc can be null, IDEA says otherwise
            if (mc == null || mc.world == null) return;

            Entity entity = mc.world.getEntityByID(entityId);
            if (entity instanceof EntityLivingBase)
                BlightHandler.markBlight((EntityLivingBase) entity, this.isBlight);
        });

        return null;
    }
}
