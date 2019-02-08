package net.silentchaos512.scalinghealth.client.gui;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.silentchaos512.lib.client.gui.DebugRenderOverlay;
import net.silentchaos512.scalinghealth.difficulty.Difficulty;

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

        // TODO: Since capabilities don't sync to client automatically, we need to do that...
        return ImmutableList.of(
                "Difficulty",
                "    Player: " + Difficulty.source(player).getDifficulty(),
                "    World: " + Difficulty.source(player.world).getDifficulty(),
                "    Area: " + Difficulty.forPos(player.world, player.getPosition())
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
