package net.silentchaos512.scalinghealth.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.network.NetworkEvent;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

import java.util.function.Supplier;

public class ClientBlightMessage {
    public int entityId;

    public ClientBlightMessage(int parent) {
        this.entityId = parent;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
    }

    public static ClientBlightMessage decode(FriendlyByteBuf buffer) {
        return new ClientBlightMessage(buffer.readInt());
    }

    public static boolean handle(ClientBlightMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity e = Minecraft.getInstance().level.getEntity(msg.entityId);
            if(e instanceof Mob)
                SHDifficulty.affected(e).setIsBlight(true);
        });
        return true;
    }
}
