package net.silentchaos512.scalinghealth.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.entity.EntityBlightFire;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SpawnBlightFirePacket {
    private int parentId;

    public SpawnBlightFirePacket() {
    }

    public SpawnBlightFirePacket(EntityLivingBase parent) {
        this.parentId = parent.getEntityId();
    }

    public static SpawnBlightFirePacket fromBytes(PacketBuffer buffer) {
        SpawnBlightFirePacket packet = new SpawnBlightFirePacket();
        packet.parentId = buffer.readVarInt();
        return packet;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeVarInt(this.parentId);
    }

    public static void handle(SpawnBlightFirePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Entity entity = getTargetEntity(packet.parentId);
            if (entity != null && entity instanceof EntityLivingBase) {
                entity.world.spawnEntity(new EntityBlightFire((EntityLivingBase) entity));
            }
        });
        context.get().setPacketHandled(true);
    }

    @Nullable
    private static Entity getTargetEntity(int entityId) {
        EntityPlayer clientPlayer = ScalingHealth.PROXY.getClientPlayer();
        if (clientPlayer == null) return null;
        return clientPlayer.world.getEntityByID(entityId);
    }
}
