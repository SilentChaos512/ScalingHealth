package net.silentchaos512.scalinghealth.client;

import net.minecraftforge.fml.network.NetworkEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.lib.AreaDifficultyMode;
import net.silentchaos512.scalinghealth.network.ClientLoginMessage;
import net.silentchaos512.scalinghealth.network.ClientSyncMessage;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.function.Supplier;

/**
 * Handles information synced from the server. Only information actually needed by the client should
 * be synced. This information will likely not be updated every tick.
 */
public final class ClientHandler {
    private static final Marker MARKER = MarkerManager.getMarker("ClientHandler");
    // Frequent updates (up to once per second)
    public static float playerDifficulty;
    public static float worldDifficulty;
    public static float areaDifficulty;
    public static int regenTimer;
    public static int locationMultiPercent;
    // Infrequent updates (join server/world, travel to new dimension)
    public static AreaDifficultyMode areaMode;
    public static float maxDifficultyValue;

    private ClientHandler() {}

    public static void onMessage(ClientSyncMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleSyncMessage(msg));
        ctx.get().setPacketHandled(true);
    }

    private static void handleSyncMessage(ClientSyncMessage msg) {
        playerDifficulty = msg.playerDifficulty;
        worldDifficulty = msg.worldDifficulty;
        areaDifficulty = msg.areaDifficulty;
        regenTimer = msg.regenTimer;
        locationMultiPercent = msg.locationMultiPercent;
    }

    public static void onLoginMessage(ClientLoginMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleLoginMessage(msg));
        ctx.get().setPacketHandled(true);
    }

    private static void handleLoginMessage(ClientLoginMessage msg) {
        ScalingHealth.LOGGER.info(MARKER, "Processing login packet");
        areaMode = msg.areaMode;
        maxDifficultyValue = msg.maxDifficultyValue;
        ScalingHealth.LOGGER.info(MARKER, "Server area mode: {}", areaMode.getDisplayName().getFormattedText());
        ScalingHealth.LOGGER.info(MARKER, "Server max difficulty: {}", maxDifficultyValue);
    }
}
