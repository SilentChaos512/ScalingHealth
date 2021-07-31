package net.silentchaos512.scalinghealth.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.resources.mechanics.*;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SHMechanicsPacket implements IntSupplier {
    private PlayerMechanics playerMechanics;
    private ItemMechanics itemMechanics;
    private MobMechanics mobMechanics;
    private DifficultyMechanics difficultyMechanics;
    private DamageScalingMechanics damageScalingMechanics;

    public SHMechanicsPacket(PlayerMechanics playerMechanics, ItemMechanics itemMechanics, MobMechanics mobMechanics, DifficultyMechanics difficultyMechanics, DamageScalingMechanics damageScalingMechanics) {
        this.playerMechanics = playerMechanics;
        this.itemMechanics = itemMechanics;
        this.mobMechanics = mobMechanics;
        this.difficultyMechanics = difficultyMechanics;
        this.damageScalingMechanics = damageScalingMechanics;
    }

    //Used magickaly (in SimpleChannel.MessageBuilder#markAsLoginPacket)
    public SHMechanicsPacket() {
        this.playerMechanics = SHMechanicListener.getPlayerMechanics();
        this.itemMechanics = SHMechanicListener.getItemMechanics();
        this.mobMechanics = SHMechanicListener.getMobMechanics();
        this.difficultyMechanics = SHMechanicListener.getDifficultyMechanics();
        this.damageScalingMechanics = SHMechanicListener.getDamageScalingMechanics();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeWithCodec(PlayerMechanics.CODEC, playerMechanics);
        buffer.writeWithCodec(ItemMechanics.CODEC, itemMechanics);
        buffer.writeWithCodec(MobMechanics.CODEC, mobMechanics);
        buffer.writeWithCodec(DifficultyMechanics.CODEC, difficultyMechanics);
        buffer.writeWithCodec(DamageScalingMechanics.CODEC, damageScalingMechanics);
    }

    public static SHMechanicsPacket decode(FriendlyByteBuf buffer) {
        PlayerMechanics playerMechanics = buffer.readWithCodec(PlayerMechanics.CODEC);;
        ItemMechanics itemMechanics = buffer.readWithCodec(ItemMechanics.CODEC);;
        MobMechanics mobMechanics = buffer.readWithCodec(MobMechanics.CODEC);;
        DifficultyMechanics difficultyMechanics = buffer.readWithCodec(DifficultyMechanics.CODEC);;
        DamageScalingMechanics damageScalingMechanics = buffer.readWithCodec(DamageScalingMechanics.CODEC);
        return new SHMechanicsPacket(playerMechanics, itemMechanics, mobMechanics, difficultyMechanics, damageScalingMechanics);
    }

    //modified from FMLHandshakeHandler#handleRegistryLoading
    public static void handle(SHMechanicsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        CountDownLatch block = new CountDownLatch(1);
        AtomicBoolean successful = new AtomicBoolean();
        ctx.get().enqueueWork(() -> {
            SHMechanicListener.setClientInstance(packet.playerMechanics, packet.itemMechanics, packet.mobMechanics, packet.difficultyMechanics, packet.damageScalingMechanics);
            successful.set(Stream.of(
                    packet.playerMechanics, packet.itemMechanics, packet.mobMechanics, packet.difficultyMechanics, packet.damageScalingMechanics
            ).allMatch(Objects::nonNull));
            block.countDown();
        });
        try {
            block.await();
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        ctx.get().setPacketHandled(true);
        if (successful.get()) {
            ScalingHealth.LOGGER.debug("Received SHMechanics on Client!");
            Network.channel.reply(new Network.SimpleReply(), ctx.get());
        }
        else {
            ScalingHealth.LOGGER.error("Failed to receive SHMechanics on Client!");
            ctx.get().getNetworkManager().disconnect(new TextComponent("Did not receive Scaling Health configuration data, closing connection."));
        }
    }

    //Copied from FMLHandshakeMessages#LoginIndexedMessage
    private int loginIdx;

    public void setLoginIdx(int idx) {
        this.loginIdx = idx;
    }

    public int getLoginIdx() {
        return this.loginIdx;
    }

    @Override
    public int getAsInt() {
        return getLoginIdx();
    }
}
