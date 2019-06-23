package net.silentchaos512.scalinghealth.client.gui;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.silentchaos512.lib.client.gui.DebugRenderOverlay;
import net.silentchaos512.scalinghealth.client.ClientHandler;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.utils.Difficulty;
import net.silentchaos512.utils.Anchor;

import javax.annotation.Nonnull;
import java.util.List;

public class DebugOverlay extends DebugRenderOverlay {
    private static final String FLOAT_FORMAT = "%.5f";

    public static DebugOverlay INSTANCE = new DebugOverlay();

    public static void init() {
        // Just need to classload this...
    }

    @Nonnull
    @Override
    public List<String> getDebugText() {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return ImmutableList.of();

        return ImmutableList.of(
                "Difficulty (/" + ClientHandler.maxDifficultyValue + ")",
                "- Mode=" + ClientHandler.areaMode.getDisplayName().getFormattedText(),
                "- Player=" + String.format(FLOAT_FORMAT, ClientHandler.playerDifficulty),
                "- World=" + String.format(FLOAT_FORMAT, ClientHandler.worldDifficulty),
                "- Area=" + String.format(FLOAT_FORMAT + " (x%.1f, ☽x%.1f)",
                        ClientHandler.areaDifficulty,
                        ClientHandler.locationMultiPercent / 100f,
                        Difficulty.lunarMultiplier(player.world)),
                "Health",
                "- Health=" + String.format("%.5f / %.1f", player.getHealth(), player.getMaxHealth()),
                "- Regen=" + String.format("%ds", ClientHandler.regenTimer / 20)
        );
    }

    @Override
    public float getTextScale() {
        return 0.75f;
    }

    @Override
    public boolean isHidden() {
        return !Config.COMMON.debugShowOverlay.get();
    }

    @Override
    public Anchor getAnchorPoint() {
        return Config.CLIENT.debugOverlayAnchor.get();
    }
}
