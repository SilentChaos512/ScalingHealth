package net.silentchaos512.scalinghealth.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.AbstractGui;

public class TextureSlice {
    private final int texU;
    private final int texV;
    private final int width;
    private final int height;

    public TextureSlice(int u, int v, int width, int height) {
        this.texU = u;
        this.texV = v;
        this.width = width;
        this.height = height;
    }

    public void blit(MatrixStack stack, int x, int y, int color, AbstractGui gui) {
        float a = ((color >> 24) & 255) / 255f;
        if (a <= 0f) a = 1f;
        float r = ((color >> 16) & 255) / 255f;
        float g = ((color >> 8) & 255) / 255f;
        float b = (color & 255) / 255f;
        RenderSystem.color4f(r, g, b, a);
        gui.blit(stack, x, y, texU, texV, width, height);
        RenderSystem.color4f(1, 1, 1, 1);
    }
}
