package net.silentchaos512.scalinghealth.client.gui;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.silentchaos512.lib.client.gui.DebugRenderOverlay;
import net.silentchaos512.scalinghealth.capability.DifficultySourceCapability;
import net.silentchaos512.scalinghealth.client.ClientHandler;
import net.silentchaos512.scalinghealth.config.SHConfig;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;
import net.silentchaos512.utils.Anchor;

import javax.annotation.Nonnull;
import java.util.List;

public class DebugOverlay extends DebugRenderOverlay {
    private static final String FLOAT_FORMAT = "%.5f";

    public static DebugOverlay INSTANCE = new DebugOverlay();

    public static void init() { }

    @Nonnull
    @Override
    public List<String> getDebugText() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return ImmutableList.of();

        return ImmutableList.of(
                "Difficulty (/" + ClientHandler.maxDifficultyValue + ")",
                "- Mode=" + ClientHandler.areaMode.getName(),
                "- Player=" + String.format(FLOAT_FORMAT, ClientHandler.playerDifficulty),
                "- Server=" + String.format(FLOAT_FORMAT, DifficultySourceCapability.getOverworldCap().orElseGet(DifficultySourceCapability::new).getDifficulty()),
                "- Area=" + String.format(FLOAT_FORMAT + " (x%.1f, ☽x%.1f)",
                        ClientHandler.areaDifficulty,
                        ClientHandler.locationMultiPercent / 100f,
                        SHDifficulty.lunarMultiplier(player.level())),
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
        return !SHConfig.SERVER.debugShowOverlay.get();
    }

    @Override
    public Anchor getAnchorPoint() {
        return SHConfig.CLIENT.debugOverlayAnchor.get();
    }
}
