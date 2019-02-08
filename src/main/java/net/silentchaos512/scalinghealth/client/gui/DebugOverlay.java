package net.silentchaos512.scalinghealth.client.gui;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.silentchaos512.lib.client.gui.DebugRenderOverlay;
import net.silentchaos512.scalinghealth.client.ClientHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class DebugOverlay extends DebugRenderOverlay {
    public static DebugOverlay INSTANCE = new DebugOverlay();

    public static void init() {
        // Just need to classload this...
    }

    @Nonnull
    @Override
    public List<String> getDebugText() {
        EntityPlayerSP player = Minecraft.getInstance().player;
        if (player == null) return ImmutableList.of();

        return ImmutableList.of(
                "Difficulty",
                "    Player: " + ClientHandler.playerDifficulty,
                "    World: " + ClientHandler.worldDifficulty,
                "    Area: " + ClientHandler.areaDifficulty
        );
    }

    @Override
    public float getTextScale() {
        return 0.75f;
    }

    @Override
    public boolean isHidden() {
        return false;
    }
}
