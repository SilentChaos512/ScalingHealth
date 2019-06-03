package net.silentchaos512.scalinghealth.network.message;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.silentchaos512.lib.event.ClientTicks;
import net.silentchaos512.scalinghealth.event.DifficultyHandler;
import net.silentchaos512.scalinghealth.network.Message;

import javax.annotation.Nullable;

public class MessageDebugData extends Message {
    public int mobsProcessed;

    public MessageDebugData() {
        this.mobsProcessed = DifficultyHandler.debugGetMobsProcessed();
    }

    @Override
    @Nullable
    @SideOnly(Side.CLIENT)
    public IMessage handleMessage(MessageContext context) {
        ClientTicks.scheduleAction(() -> DifficultyHandler.debugHandleSyncMessage(this));
        return null;
    }
}
