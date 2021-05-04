package net.silentchaos512.scalinghealth.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.client.ClientHandler;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

import java.util.Objects;

public final class Network {
    private static final ResourceLocation NAME = ScalingHealth.getId("network");

    public static SimpleChannel channel;

    public static void init() {
        channel = NetworkRegistry.ChannelBuilder.named(NAME)
                .clientAcceptedVersions(s -> Objects.equals(s, "1.1"))
                .serverAcceptedVersions(s -> Objects.equals(s, "1.1"))
                .networkProtocolVersion(() -> "1.1")
                .simpleChannel();

        channel.messageBuilder(ClientSyncMessage.class, 1)
                .decoder(ClientSyncMessage::fromBytes)
                .encoder(ClientSyncMessage::toBytes)
                .consumer(ClientHandler::onMessage)
                .add();

        channel.messageBuilder(ClientLoginMessage.class, 2)
                .decoder(ClientLoginMessage::fromBytes)
                .encoder(ClientLoginMessage::toBytes)
                .consumer(ClientHandler::onLoginMessage)
                .add();

        channel.messageBuilder(ClientBlightMessage.class, 3)
                .decoder(buffer -> new ClientBlightMessage(buffer.readInt()))
                .encoder((msg, buffer)-> buffer.writeInt(msg.entityId))
                .consumer((msg, ctx) -> {
                    ctx.get().enqueueWork(()-> {
                        Entity e = Minecraft.getInstance().world.getEntityByID(msg.entityId);
                        if(e instanceof MobEntity)
                            SHDifficulty.affected(e).setIsBlight(true);
                    });
                    return true;
                })
                .add();
    }
}
