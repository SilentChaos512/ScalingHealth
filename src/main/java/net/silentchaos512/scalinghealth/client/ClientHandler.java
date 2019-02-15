package net.silentchaos512.scalinghealth.client;

import net.minecraftforge.fml.network.NetworkEvent;
import net.silentchaos512.scalinghealth.network.ClientSyncMessage;

import java.util.function.Supplier;

/**
 * Handles information synced from the server. Only information actually needed by the client should
 * be synced. This information will likely not be updated every tick.
 */
public final class ClientHandler {
    public static float playerDifficulty;
    public static float worldDifficulty;
    public static float areaDifficulty;

    private ClientHandler() {}

    public static void onMessage(ClientSyncMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            playerDifficulty = msg.playerDifficulty;
            worldDifficulty = msg.worldDifficulty;
            areaDifficulty = msg.areaDifficulty;
        });
        ctx.get().setPacketHandled(true);
    }
}
