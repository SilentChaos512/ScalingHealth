package net.silentchaos512.scalinghealth.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.resources.mechanics.*;

import java.io.IOException;
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

    public void encode(PacketBuffer buffer) {
        try {
            buffer.func_240629_a_(PlayerMechanics.CODEC, playerMechanics);
            buffer.func_240629_a_(ItemMechanics.CODEC, itemMechanics);
            buffer.func_240629_a_(MobMechanics.CODEC, mobMechanics);
            buffer.func_240629_a_(DifficultyMechanics.CODEC, difficultyMechanics);
            buffer.func_240629_a_(DamageScalingMechanics.CODEC, damageScalingMechanics);
        } catch (IOException e) {
            ScalingHealth.LOGGER.error(e);
        }
    }

    public static SHMechanicsPacket decode(PacketBuffer buffer) {
        PlayerMechanics playerMechanics = null;
        ItemMechanics itemMechanics = null;
        MobMechanics mobMechanics = null;
        DifficultyMechanics difficultyMechanics = null;
        DamageScalingMechanics damageScalingMechanics = null;
        try {
            playerMechanics = buffer.func_240628_a_(PlayerMechanics.CODEC);
            itemMechanics = buffer.func_240628_a_(ItemMechanics.CODEC);
            mobMechanics = buffer.func_240628_a_(MobMechanics.CODEC);
            difficultyMechanics = buffer.func_240628_a_(DifficultyMechanics.CODEC);
            damageScalingMechanics = buffer.func_240628_a_(DamageScalingMechanics.CODEC);
        } catch (IOException e) {
            ScalingHealth.LOGGER.error(e);
        }
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
            ctx.get().getNetworkManager().closeChannel(new StringTextComponent("Did not receive Scaling Health configuration data, closing connection."));
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
