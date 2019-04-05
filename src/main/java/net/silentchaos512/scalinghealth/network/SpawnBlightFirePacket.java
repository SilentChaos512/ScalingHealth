package net.silentchaos512.scalinghealth.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;
import net.silentchaos512.scalinghealth.entity.EntityBlightFire;

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

    @OnlyIn(Dist.CLIENT)
    public static void handle(SpawnBlightFirePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().world.getEntityByID(packet.parentId);
            if (entity != null && entity instanceof EntityLivingBase) {
                entity.world.spawnEntity(new EntityBlightFire((EntityLivingBase) entity));
            }
        });
        context.get().setPacketHandled(true);
    }
}
