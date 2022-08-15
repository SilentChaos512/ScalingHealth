package net.silentchaos512.scalinghealth.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.HandshakeHandler;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.client.ClientHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.Objects;
import java.util.function.IntSupplier;

public final class Network {
    private static final ResourceLocation NAME = ScalingHealth.getId("network");

    public static SimpleChannel channel;

    public static void init() {
        channel = NetworkRegistry.ChannelBuilder.named(NAME)
                .clientAcceptedVersions(s -> Objects.equals(s, "3"))
                .serverAcceptedVersions(s -> Objects.equals(s, "3"))
                .networkProtocolVersion(() -> "3")
                .simpleChannel();

        channel.messageBuilder(ClientSyncMessage.class, 1)
                .decoder(ClientSyncMessage::fromBytes)
                .encoder(ClientSyncMessage::toBytes)
                .consumer(ClientHandler::handleSyncMessage)
                .add();

        channel.messageBuilder(ClientLoginMessage.class, 2)
                .decoder(ClientLoginMessage::fromBytes)
                .encoder(ClientLoginMessage::toBytes)
                .consumer(ClientHandler::handleLoginMessage)
                .add();

        channel.messageBuilder(ClientBlightMessage.class, 3)
                .decoder(ClientBlightMessage::decode)
                .encoder(ClientBlightMessage::encode)
                .consumer(ClientBlightMessage::handle)
                .add();

        channel.messageBuilder(SHMechanicsPacket.class, 4)
                .decoder(SHMechanicsPacket::decode)
                .encoder(SHMechanicsPacket::encode)
                .consumer(SHMechanicsPacket::handle)
                .add();
    }
}
