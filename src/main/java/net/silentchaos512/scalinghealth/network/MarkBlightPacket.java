package net.silentchaos512.scalinghealth.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.event.BlightHandler;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class MarkBlightPacket {
    private int entity;

    public MarkBlightPacket() {
    }

    public MarkBlightPacket(MobEntity entity) {
        this.entity = entity.getEntityId();
    }

    public static MarkBlightPacket fromBytes(PacketBuffer buffer) {
        MarkBlightPacket packet = new MarkBlightPacket();
        packet.entity = buffer.readVarInt();
        return packet;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeVarInt(this.entity);
    }

    public static void handle(MarkBlightPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Entity e = getTargetEntity(packet.entity);
            if(e instanceof MobEntity){
                BlightHandler.markBlight((MobEntity) e);
                //ScalingHealth.LOGGER.debug("Handling the MarkBlight Packet for {}", e.getName().getString());
            }
        });
        context.get().setPacketHandled(true);
    }

    @Nullable
    private static Entity getTargetEntity(int entityId) {
        PlayerEntity clientPlayer = ScalingHealth.PROXY.getClientPlayer();
        if (clientPlayer == null) return null;
        return clientPlayer.world.getEntityByID(entityId);
    }
}
