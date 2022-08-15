package net.silentchaos512.scalinghealth.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.silentchaos512.scalinghealth.client.MechanicsHandler;
import net.silentchaos512.scalinghealth.resources.mechanics.*;

import java.util.function.Supplier;

public record SHMechanicsPacket(SHMechanics shMechanics) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeWithCodec(PlayerMechanics.CODEC, shMechanics.playerMechanics());
        buffer.writeWithCodec(ItemMechanics.CODEC, shMechanics.itemMechanics());
        buffer.writeWithCodec(MobMechanics.CODEC, shMechanics.mobMechanics());
        buffer.writeWithCodec(DifficultyMechanics.CODEC, shMechanics.difficultyMechanics());
        buffer.writeWithCodec(DamageScalingMechanics.CODEC, shMechanics.damageScalingMechanics());
    }

    public static SHMechanicsPacket decode(FriendlyByteBuf buffer) {
        return new SHMechanicsPacket(SHMechanics.fromNetwork(buffer));
    }

    public static boolean handle(SHMechanicsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> MechanicsHandler.setClientMechanics(packet.shMechanics));
        return true;
    }
}
