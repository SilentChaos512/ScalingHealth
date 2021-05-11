package net.silentchaos512.scalinghealth.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.FMLHandshakeHandler;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
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
                .clientAcceptedVersions(s -> Objects.equals(s, "2"))
                .serverAcceptedVersions(s -> Objects.equals(s, "2"))
                .networkProtocolVersion(() -> "2")
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

        channel.messageBuilder(SHMechanicsPacket.class, 4, NetworkDirection.LOGIN_TO_CLIENT)
                .decoder(SHMechanicsPacket::decode)
                .encoder(SHMechanicsPacket::encode)
                .consumer(SHMechanicsPacket::handle)
                //from #markAsLoginPacket but modified to not send anything on local connections
                .buildLoginPacketList(isLocal -> isLocal ? Collections.emptyList() :
                        Collections.singletonList(Pair.of(SHMechanicsPacket.class.getName(), new SHMechanicsPacket()))
                )
                .loginIndex(SHMechanicsPacket::getLoginIdx, SHMechanicsPacket::setLoginIdx)
                .add();

        channel.messageBuilder(SimpleReply.class, 5, NetworkDirection.LOGIN_TO_SERVER)
                .decoder(pb -> new SimpleReply())
                .encoder((r, pb) -> {})
                .consumer(FMLHandshakeHandler.indexFirst((handler, pkt, ctx) -> ctx.get().setPacketHandled(true)))
                .loginIndex(SimpleReply::getIdx, SimpleReply::setIdx)
                .add();
    }

    public static class SimpleReply implements IntSupplier {
        private int idx;

        @Override
        public int getAsInt() {
            return getIdx();
        }

        public int getIdx() {
            return idx;
        }

        public void setIdx(int idx) {
            this.idx = idx;
        }
    }
}
