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

package net.silentchaos512.scalinghealth.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.silentchaos512.lib.event.ClientTicks;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.lib.EnumAreaDifficultyMode;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public class DifficultyDisplayHandler extends Gui {
    public static final DifficultyDisplayHandler INSTANCE = new DifficultyDisplayHandler();

    private static final ResourceLocation TEXTURE = new ResourceLocation(ScalingHealth.MOD_ID_LOWER, "textures/gui/hud.png");

    private int lastDifficultyDisplayed = -100;
    private int lastAreaDifficultyDisplayed = -100;
    private int lastUpdateTime = Integer.MIN_VALUE;

    public void showBar() {
        this.lastUpdateTime = Integer.MIN_VALUE;
        this.lastDifficultyDisplayed = -100;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != ElementType.TEXT || Config.Difficulty.maxValue <= 0 || !Config.Client.Difficulty.renderMeter)
            return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;

        int width = event.getResolution().getScaledWidth();
        int height = event.getResolution().getScaledHeight();

        PlayerData data = player != null ? SHPlayerDataHandler.get(player) : null;
        if (data == null)
            return;

        int difficulty = (int) data.getDifficulty();
        int areaDifficultyUnclamped = (int) Config.Difficulty.AREA_DIFFICULTY_MODE.getAreaDifficulty(player.world, player.getPosition());
        int areaDifficulty = MathHelper.clamp(areaDifficultyUnclamped, 0, (int) Config.Difficulty.maxValue);
        if (Config.Difficulty.AREA_DIFFICULTY_MODE == EnumAreaDifficultyMode.SERVER_WIDE) {
            difficulty = areaDifficulty;
        }
        int timeSinceLastUpdate = ClientTicks.ticksInGame - lastUpdateTime;

        if (difficulty != lastDifficultyDisplayed) {
            lastDifficultyDisplayed = difficulty;
            lastUpdateTime = ClientTicks.ticksInGame;
        }
        if (areaDifficulty < lastAreaDifficultyDisplayed - 10 || areaDifficulty > lastAreaDifficultyDisplayed + 10 && timeSinceLastUpdate > 1200) {
            lastAreaDifficultyDisplayed = areaDifficulty;
            lastUpdateTime = ClientTicks.ticksInGame;
        }

        int currentTime = ClientTicks.ticksInGame;
        if (Config.Client.Difficulty.renderMeterAlways || currentTime - lastUpdateTime < Config.Client.Difficulty.meterDisplayTime) {
            GlStateManager.enableBlend();

            mc.renderEngine.bindTexture(TEXTURE);

            GlStateManager.pushMatrix();
            // GlStateManager.scale(1f, 0.5f, 1f);

            int posX = Config.Client.Difficulty.meterPosX; // 5;
            if (posX < 0)
                posX = posX + width - 64;
            int posY = Config.Client.Difficulty.meterPosY; // height - 30;
            if (posY < 0)
                posY = posY + height - 12;

            // Frame
            drawTexturedModalRect(posX, posY, 190, 0, 66, 14, 0xFFFFFF);

            // Area Difficulty
            int barLength = (int) (60 * areaDifficulty / Config.Difficulty.maxValue);
            drawTexturedModalRect(posX + 3, posY + 5, 193, 19, barLength, 6, 0xFFFFFF);

            // Player Difficulty
            barLength = (int) (60 * difficulty / Config.Difficulty.maxValue);
            drawTexturedModalRect(posX + 3, posY + 3, 193, 17, barLength, 2, 0xFFFFFF);

            // Text
            GlStateManager.pushMatrix();
            float textScale = 0.6f;
            GlStateManager.scale(textScale, textScale, 1.0f);
            String localizedString = ScalingHealth.i18n.miscText("difficultyMeterText");
            mc.fontRenderer.drawStringWithShadow(localizedString, posX / textScale + 4, posY / textScale - 9, 0xFFFFFF);
            // Text Difficulty
            String str = String.format("%d", areaDifficultyUnclamped);
            int strWidth = mc.fontRenderer.getStringWidth(str);
            mc.fontRenderer.drawStringWithShadow(str, posX / textScale + 104 - strWidth, posY / textScale - 9, 0xAAAAAA);
            GlStateManager.popMatrix();

            GlStateManager.popMatrix();
            GlStateManager.disableBlend();
        }
    }

    private void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height, int color) {
        float r = ((color >> 16) & 255) / 255f;
        float g = ((color >> 8) & 255) / 255f;
        float b = (color & 255) / 255f;
        GlStateManager.color(r, g, b);
        drawTexturedModalRect(x, y, textureX, textureY, width, height);
        GlStateManager.color(1f, 1f, 1f);
    }
}
