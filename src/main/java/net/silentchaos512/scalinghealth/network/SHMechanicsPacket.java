package net.silentchaos512.scalinghealth.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.silentchaos512.scalinghealth.client.MechanicsHandler;
import net.silentchaos512.scalinghealth.resources.mechanics.*;

import java.util.function.Supplier;

public record SHMechanicsPacket(SHMechanics shMechanics) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeJsonWithCodec(PlayerMechanics.CODEC, shMechanics.playerMechanics());
        buffer.writeJsonWithCodec(ItemMechanics.CODEC, shMechanics.itemMechanics());
        buffer.writeJsonWithCodec(MobMechanics.CODEC, shMechanics.mobMechanics());
        buffer.writeJsonWithCodec(DifficultyMechanics.CODEC, shMechanics.difficultyMechanics());
        buffer.writeJsonWithCodec(DamageScalingMechanics.CODEC, shMechanics.damageScalingMechanics());
    }

    public static SHMechanicsPacket decode(FriendlyByteBuf buffer) {
        return new SHMechanicsPacket(SHMechanics.fromNetwork(buffer));
    }

    public static void handle(SHMechanicsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        MechanicsHandler.setClientMechanics(packet.shMechanics);
    }
}
