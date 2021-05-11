package net.silentchaos512.scalinghealth.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

import java.util.function.Supplier;

public class ClientBlightMessage {
    public int entityId;

    public ClientBlightMessage(int parent) {
        this.entityId = parent;
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeInt(entityId);
    }

    public static ClientBlightMessage decode(PacketBuffer buffer) {
        return new ClientBlightMessage(buffer.readInt());
    }

    public static boolean handle(ClientBlightMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity e = Minecraft.getInstance().world.getEntityByID(msg.entityId);
            if(e instanceof MobEntity)
                SHDifficulty.affected(e).setIsBlight(true);
        });
        return true;
    }
}
