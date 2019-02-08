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

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;

public class DifficultyMeter extends Gui {
    public static final DifficultyMeter INSTANCE = new DifficultyMeter();

    private static final ResourceLocation TEXTURE = new ResourceLocation(ScalingHealth.MOD_ID, "textures/gui/hud.png");

    private int lastDifficultyDisplayed = -100;
    private int lastAreaDifficultyDisplayed = -100;
    private int lastUpdateTime = Integer.MIN_VALUE;

    public void showBar() {
        this.lastUpdateTime = Integer.MIN_VALUE;
        this.lastDifficultyDisplayed = -100;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        /*
        DifficultyMeterShow showMode = Config.CLIENT.difficultyMeterShow.get();
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT
                || showMode == DifficultyMeterShow.NEVER) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        EntityPlayer player = mc.player;
        if (player == null) return;

        DimensionConfig config = Config.get(player);
        final double maxDifficulty = config.difficulty.maxValue.get();
        if (maxDifficulty <= 0) return;

        int width = event.getResolution().getScaledWidth();
        int height = event.getResolution().getScaledHeight();

        SHPlayerDataHandler.PlayerData data = SHPlayerDataHandler.get(player);
        if (data == null) return;

        AreaDifficultyMode areaMode = config.difficulty.areaMode.get();
        int preClampAreaDifficulty = (int) areaMode.getAreaDifficulty(player.world, player.getPosition());
        int areaDifficulty = MathHelper.clamp(preClampAreaDifficulty, 0, (int) maxDifficulty);
        int difficulty = areaMode == AreaDifficultyMode.DIMENSION_WIDE
                ? areaDifficulty
                : (int) data.getDifficulty();
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
        if (showMode == DifficultyMeterShow.ALWAYS || currentTime - lastUpdateTime < Config.CLIENT.difficultyMeterShowTime.get()) {
            GlStateManager.enableBlend();

            mc.textureManager.bindTexture(TEXTURE);

            GlStateManager.pushMatrix();
            // GlStateManager.scale(1f, 0.5f, 1f);

            HudAnchor anchor = Config.CLIENT.difficultyMeterAnchor.get();
            int posX = anchor.offsetX(width, Config.CLIENT.difficultyMeterOffsetX.get());
            int posY = anchor.offsetY(height, Config.CLIENT.difficultyMeterOffsetY.get());

            // Frame
            drawTexturedModalRect(posX, posY, 190, 0, 66, 14, 0xFFFFFF);

            // Area Difficulty
            int barLength = (int) (60 * areaDifficulty / maxDifficulty);
            drawTexturedModalRect(posX + 3, posY + 5, 193, 19, barLength, 6, 0xFFFFFF);

            // Player Difficulty
            barLength = (int) (60 * difficulty / maxDifficulty);
            drawTexturedModalRect(posX + 3, posY + 3, 193, 17, barLength, 2, 0xFFFFFF);

            // Text
            float textScale = 0.6f;
            GlStateManager.pushMatrix();
            GlStateManager.scalef(textScale, textScale, 1);
            ITextComponent text = new TextComponentTranslation("misc.scalinghealth.difficultyMeterText");
            mc.fontRenderer.drawStringWithShadow(
                    text.getFormattedText(),
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

            GlStateManager.popMatrix();
            GlStateManager.disableBlend();
        }
        */
    }

    private void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height, int color) {
        float r = ((color >> 16) & 255) / 255f;
        float g = ((color >> 8) & 255) / 255f;
        float b = (color & 255) / 255f;
        GlStateManager.color3f(r, g, b);
        drawTexturedModalRect(x, y, textureX, textureY, width, height);
        GlStateManager.color3f(1, 1, 1);
    }
}
