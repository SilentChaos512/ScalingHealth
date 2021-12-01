package net.silentchaos512.scalinghealth.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.network.ClientLoginMessage;
import net.silentchaos512.scalinghealth.network.ClientSyncMessage;
import net.silentchaos512.scalinghealth.utils.mode.AreaDifficultyMode;
import net.silentchaos512.scalinghealth.utils.mode.AreaDifficultyModes;
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
    public static float areaDifficulty;
    public static int regenTimer;
    public static int locationMultiPercent;
    // Infrequent updates (join server/world, travel to new dimension)
    public static AreaDifficultyMode areaMode = AreaDifficultyModes.ServerWide.INSTANCE;
    public static float maxDifficultyValue;

    private ClientHandler() {}

    public static void handleSyncMessage(ClientSyncMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            playerDifficulty = msg.playerDifficulty;
            areaDifficulty = msg.areaDifficulty;
            regenTimer = msg.regenTimer;
            locationMultiPercent = msg.locationMultiPercent;

            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player != null) {
                player.experienceLevel = msg.experienceLevel;
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void handleLoginMessage(ClientLoginMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ScalingHealth.LOGGER.info(MARKER, "Processing login packet");
            areaMode = msg.areaMode;
            maxDifficultyValue = msg.maxDifficultyValue;
            ScalingHealth.LOGGER.info(MARKER, "World area mode: {}", areaMode.getName());
            ScalingHealth.LOGGER.info(MARKER, "World max difficulty: {}", maxDifficultyValue);
        });
        ctx.get().setPacketHandled(true);
    }
}
