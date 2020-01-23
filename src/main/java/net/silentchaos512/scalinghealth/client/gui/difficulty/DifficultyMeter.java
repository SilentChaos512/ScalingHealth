/*
 * Scaling Health
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.scalinghealth.client.gui.difficulty;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.silentchaos512.lib.event.ClientTicks;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.client.ClientHandler;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.lib.AreaDifficultyMode;
import net.silentchaos512.utils.Anchor;

public class DifficultyMeter extends Screen {
    public static final DifficultyMeter INSTANCE = new DifficultyMeter(new StringTextComponent(""));

    private static final ResourceLocation TEXTURE = new ResourceLocation(ScalingHealth.MOD_ID, "textures/gui/hud.png");

    private int lastDifficultyDisplayed = -100;
    private int lastAreaDifficultyDisplayed = -100;
    private int lastUpdateTime = Integer.MIN_VALUE;

    protected DifficultyMeter(ITextComponent title) {
        super(title);
    }

    public void showBar() {
        this.lastUpdateTime = Integer.MIN_VALUE;
        this.lastDifficultyDisplayed = -100;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        DifficultyMeterShow showMode = Config.CLIENT.difficultyMeterShow.get();
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT|| showMode == DifficultyMeterShow.NEVER) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        if (player == null) return;

        final double maxDifficulty = ClientHandler.maxDifficultyValue;
        if (maxDifficulty <= 0) return;

        int width = mc.mainWindow.getScaledWidth();
        int height = mc.mainWindow.getScaledHeight();

        AreaDifficultyMode areaMode = ClientHandler.areaMode;
        int preClampAreaDifficulty = (int) ClientHandler.areaDifficulty;
        int areaDifficulty = MathHelper.clamp(preClampAreaDifficulty, 0, (int) maxDifficulty);
        int difficulty = areaMode == AreaDifficultyMode.DIMENSION_WIDE
                ? areaDifficulty
                : (int) ClientHandler.playerDifficulty;
        int timeSinceLastUpdate = ClientTicks.ticksInGame() - lastUpdateTime;

        if (difficulty != lastDifficultyDisplayed) {
            lastDifficultyDisplayed = difficulty;
            lastUpdateTime = ClientTicks.ticksInGame();
        }
        if (areaDifficulty < lastAreaDifficultyDisplayed - 10 || areaDifficulty > lastAreaDifficultyDisplayed + 10 && timeSinceLastUpdate > 1200) {
            lastAreaDifficultyDisplayed = areaDifficulty;
            lastUpdateTime = ClientTicks.ticksInGame();
        }

        int currentTime = ClientTicks.ticksInGame();
        if (showMode == DifficultyMeterShow.ALWAYS || currentTime - lastUpdateTime < 20 * Config.CLIENT.difficultyMeterShowTime.get()) {
            GlStateManager.enableBlend();

            mc.textureManager.bindTexture(TEXTURE);

            GlStateManager.pushMatrix();
            // GlStateManager.scale(1f, 0.5f, 1f);

            Anchor anchor = Config.CLIENT.difficultyMeterAnchor.get();
            int posX = anchor.getX(width, 66, 5) + Config.CLIENT.difficultyMeterOffsetX.get();
            int posY = anchor.getY(height, 14, 5) + Config.CLIENT.difficultyMeterOffsetY.get();

            // Frame
            blitWithColor(posX, posY, 190, 0, 66, 14, 0xFFFFFF);

            // Area Difficulty
            int barLength = (int) (60 * areaDifficulty / maxDifficulty);
            blitWithColor(posX + 3, posY + 5, 193, 19, barLength, 6, 0xFFFFFF);

            // Player Difficulty
            barLength = (int) (60 * difficulty / maxDifficulty);
            blitWithColor(posX + 3, posY + 3, 193, 17, barLength, 2, 0xFFFFFF);

            // Text
            final float textScale = Config.CLIENT.difficultyMeterTextScale.get().floatValue();
            if (textScale > 0) {
                GlStateManager.pushMatrix();
                GlStateManager.scalef(textScale, textScale, 1);
                mc.fontRenderer.drawStringWithShadow(
                        I18n.format("misc.scalinghealth.difficultyMeterText"),
                        posX / textScale + 4,
                        posY / textScale - 9,
                        0xFFFFFF);

                // Text Difficulty
                String str = String.format("%d", areaDifficulty);
                int strWidth = mc.fontRenderer.getStringWidth(str);
                mc.fontRenderer.drawStringWithShadow(str,
                        posX / textScale + 104 - strWidth,
                        posY / textScale - 9,
                        0xAAAAAA);
                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();
            GlStateManager.disableBlend();
        }
    }

    private void blitWithColor(int x, int y, int textureX, int textureY, int width, int height, int color) {
        float r = ((color >> 16) & 255) / 255f;
        float g = ((color >> 8) & 255) / 255f;
        float b = (color & 255) / 255f;
        GlStateManager.color3f(r, g, b);
        blit(x, y, textureX, textureY, width, height);
        GlStateManager.color3f(1, 1, 1);
    }
}
