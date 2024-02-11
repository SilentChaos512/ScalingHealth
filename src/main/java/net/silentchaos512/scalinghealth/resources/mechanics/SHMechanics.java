package net.silentchaos512.scalinghealth.resources.mechanics;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkDirection;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.client.MechanicsHandler;
import net.silentchaos512.scalinghealth.network.ClientLoginMessage;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.network.SHMechanicsPacket;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public record SHMechanics(PlayerMechanics playerMechanics, ItemMechanics itemMechanics, MobMechanics mobMechanics, DifficultyMechanics difficultyMechanics, DamageScalingMechanics damageScalingMechanics) {
    public static final SHMechanics DEFAULT = new SHMechanics(
            PlayerMechanics.DEFAULT, ItemMechanics.DEFAULT, MobMechanics.DEFAULT, DifficultyMechanics.DEFAULT, DamageScalingMechanics.DEFAULT
    );

    public static SHMechanics fromNetwork(FriendlyByteBuf buffer) {
        return new SHMechanics(
                buffer.readWithCodec(PlayerMechanics.CODEC),
                buffer.readWithCodec(ItemMechanics.CODEC),
                buffer.readWithCodec(MobMechanics.CODEC),
                buffer.readWithCodec(DifficultyMechanics.CODEC),
                buffer.readWithCodec(DamageScalingMechanics.CODEC)
        );
    }

    public static SHMechanics getMechanics() {
        return DistExecutor.unsafeRunForDist( //unsafe faster
                () -> Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER
                        ? SHMechanicListener::getInstance //use server-side config in singleplayer
                        : MechanicsHandler::getClientMechanics,
                () -> SHMechanicListener::getInstance
        );
    }

    @SubscribeEvent
    public static void syncMechanics(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            Network.channel.sendTo(new SHMechanicsPacket(SHMechanicListener.getInstance()), event.getPlayer().connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            Network.channel.sendTo(new ClientLoginMessage(SHDifficulty.areaMode(), (float) SHDifficulty.maxValue()), event.getPlayer().connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
        else {
            for (ServerPlayer player : event.getPlayerList().getPlayers()) {
                Network.channel.sendTo(new SHMechanicsPacket(SHMechanicListener.getInstance()), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                Network.channel.sendTo(new ClientLoginMessage(SHDifficulty.areaMode(), (float) SHDifficulty.maxValue()), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }
}
