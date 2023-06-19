package net.silentchaos512.scalinghealth.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class TextureSlice {
    private final int texU;
    private final int texV;
    private final int width;
    private final int height;
    private final ResourceLocation texture;

    public TextureSlice(ResourceLocation texture, int u, int v, int width, int height) {
        this.texU = u;
        this.texV = v;
        this.width = width;
        this.height = height;
        this.texture = texture;
    }

    public void blit(GuiGraphics graphics, int x, int y, int color) {
        float a = ((color >> 24) & 255) / 255f;
        if (a <= 0f) a = 1f;
        float r = ((color >> 16) & 255) / 255f;
        float g = ((color >> 8) & 255) / 255f;
        float b = (color & 255) / 255f;
        graphics.setColor(r, g, b, a);
        graphics.blit(texture, x, y, texU, texV, width, height);
        graphics.setColor(1, 1, 1, 1);
    }
}
