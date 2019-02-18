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

package net.silentchaos512.scalinghealth.client.gui.health;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.silentchaos512.lib.event.ClientTicks;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.utils.Color;
import net.silentchaos512.utils.MathUtils;

import java.util.List;

/**
 * Handles display of regular and absorption hearts.
 * <p>For future reference, much of the vanilla code can be found in {@link GuiIngameForge}.
 */
public final class HeartDisplayHandler extends Gui {
    public static final HeartDisplayHandler INSTANCE = new HeartDisplayHandler();

    private static final float COLOR_CHANGE_PERIOD = 150;
    private static final ResourceLocation TEXTURE = new ResourceLocation(ScalingHealth.MOD_ID, "textures/gui/hud.png");

    private final HeartsInfo info = new HeartsInfo();

    private HeartDisplayHandler() {}

    @SubscribeEvent(receiveCanceled = true)
    public void onHealthBar(RenderGameOverlayEvent.Pre event) {
        if (info.heartStyle == HeartIconStyle.VANILLA) return;

        Minecraft mc = Minecraft.getInstance();
        EntityPlayer player = mc.player;

        // Health text
        if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT && mc.playerController.gameIsSurvivalOrAdventure()) {
            // Draw health string?
            if (Config.CLIENT.healthTextStyle.get() != HealthTextStyle.DISABLED) {
                mc.profiler.startSection("scalinghealthRenderHealthText");
                renderHealthText(mc, info.health, info.maxHealth,
                        -91 + Config.CLIENT.healthTextOffsetX.get(),
                        -38 + Config.CLIENT.healthTextOffsetY.get(),
                        Config.CLIENT.healthTextStyle.get(),
                        Config.CLIENT.healthTextColorStyle.get());
                mc.profiler.endSection();
            }
            // Draw absorption amount string?
            if (Config.CLIENT.absorptionTextStyle.get() != HealthTextStyle.DISABLED && player.getAbsorptionAmount() > 0) {
                mc.profiler.startSection("scalinghealthRenderAbsorptionText");
                renderHealthText(mc, player.getAbsorptionAmount(), 0,
                        -91 + Config.CLIENT.absorptionTextOffsetX.get(),
                        -49 + Config.CLIENT.absorptionTextOffsetY.get(),
                        Config.CLIENT.absorptionTextStyle.get(),
                        HealthTextColor.SOLID);
                mc.profiler.endSection();
            }
        }

        // Hearts
        if (event.getType() == RenderGameOverlayEvent.ElementType.HEALTH && info.heartStyle != HeartIconStyle.VANILLA) {
            event.setCanceled(true);
            mc.profiler.startSection("scalinghealthRenderHearts");
            renderHearts(event, mc, player);
            mc.profiler.endSection();
        }
    }

    private void renderHearts(RenderGameOverlayEvent event, Minecraft mc, EntityPlayer player) {
        info.update();

        GlStateManager.enableBlend();

        float absorb = MathHelper.ceil(player.getAbsorptionAmount());

        final int left = info.scaledWindowWidth / 2 - 91;
        final int top = info.scaledWindowHeight - GuiIngameForge.left_height;
        GuiIngameForge.left_height += info.rowsUsedInHud * info.rowHeight;
        if (info.rowHeight != 10)
            GuiIngameForge.left_height += 10 - info.rowHeight;

        // Draw vanilla hearts
        drawVanillaHearts(left, top);

        int potionOffset = info.hardcoreMode ? 27 : 0;

        // Draw extra hearts (only top 2 rows)
        mc.textureManager.bindTexture(TEXTURE);
        int rowCount = info.getCustomHeartRowCount(info.healthInt);
        int maxHealthRows = info.getCustomHeartRowCount((int) player.getMaxHealth());

        for (int row = Math.max(0, rowCount - 2); row < rowCount; ++row) {
            int actualRow = info.getActualRow(row);
            int renderHearts = info.getHeartsInRows(actualRow);
            int rowColor = getColorForRow(row, false);

            // Draw the hearts
            int j;
            for (j = 0; j < renderHearts; ++j) {
                int y = info.offsetHeartPosY(j, top);
                drawTexturedModalRect(left + 8 * j, y, 0, potionOffset, 9, 9, rowColor);
            }
            boolean anythingDrawn = j > 0;

            // Half heart on the end?
            if (info.healthInt % 2 == 1 && renderHearts < 10) {
                int y = info.offsetHeartPosY(j, top);
                drawTexturedModalRect(left + 8 * renderHearts, y, 9, potionOffset, 9, 9, rowColor);
                anythingDrawn = true;
            }

            // Outline for last heart, to make seeing max health a little easier.
            if (Config.CLIENT.lastHeartOutline.get() && anythingDrawn && row == maxHealthRows - 1) {
                // Get position of last partial/full heart
                j = (int) (Math.ceil(player.getMaxHealth() % 20f / 2f)) - 1;
                if (j < 0) j += 10;
                int y = info.offsetHeartPosY(j, top);
                int color = Config.CLIENT.lastHeartOutlineColor.get();
                drawTexturedModalRect(left + 8 * j, y, 17, 9, 9, 9, color);
            }
        }

        for (int i = 0; i < 10 && i < Math.ceil(info.healthInt / 2f); ++i) {
            int y = info.offsetHeartPosY(i, top);
            // Effect hearts (poison, wither)
            if (showEffectHearts(player)) {
                int color = effectHeartColor(player);
                drawTexturedModalRect(left + 8 * i, y, 0, 54, 9, 9, color);
            }
            // Shiny glint on top of the hearts, a single white pixel in the upper left <3
            if (!info.hardcoreMode) {
                drawTexturedModalRect(left + 8 * i, y, 17, 0, 9, 9, 0xCCFFFFFF);
            }
        }

        // ==========================
        // Absorption hearts override
        // ==========================

        AbsorptionIconStyle absorptionIconStyle = Config.CLIENT.absorptionIconStyle.get();
        if (absorptionIconStyle != AbsorptionIconStyle.VANILLA) {
            int absorbCeil = (int) Math.ceil(absorb);
            rowCount = (int) Math.ceil(absorb / 20);

            // Dark underlay for first row
            int texX = 17;
            int texY = absorptionIconStyle == AbsorptionIconStyle.SHIELD ? 45 : 54;
            for (int i = 0; i < 10 && i < absorb / 2; ++i) {
                int y = info.offsetAbsorptionPosY(i, top);
                drawTexturedModalRect(left + 8 * i, y, texX, texY, 9, 9, 0xFFFFFF);
            }

            // Draw the top two absorption rows, just the basic "hearts"
            texX = absorptionIconStyle == AbsorptionIconStyle.SHIELD ? 26 : 0;
            texY = absorptionIconStyle == AbsorptionIconStyle.SHIELD ? 0 : potionOffset;
            for (int i = Math.max(0, rowCount - 2); i < rowCount; ++i) {
                int renderHearts = Math.min((absorbCeil - 20 * i) / 2, 10);
                int rowColor = getColorForRow(i, true);
                boolean anythingDrawn;

                // Draw the hearts
                int x;
                for (x = 0; x < renderHearts; ++x) {
                    int y = info.offsetAbsorptionPosY(x, top);
                    drawTexturedModalRect(left + 8 * x, y, texX, texY, 9, 9, rowColor);
                }
                anythingDrawn = x > 0;

                // Half heart on the end?
                if (absorbCeil % 2 == 1 && renderHearts < 10) {
                    int y = info.offsetAbsorptionPosY(x, top);
                    drawTexturedModalRect(left + 8 * renderHearts, y, texX + 9, texY, 9, 9, rowColor);
                    anythingDrawn = true;
                }
            }

            // Add extra bits like outlines on top
            for (int i = 0; i < 10 && i < absorb / 2; ++i) {
                int y = info.offsetAbsorptionPosY(i, top);
                if (absorptionIconStyle == AbsorptionIconStyle.SHIELD) {
                    // Golden hearts in center (shield style only)
                    drawTexturedModalRect(left + 8 * i, y, 17, 36, 9, 9, 0xFFFFFF);
                } else if (absorptionIconStyle == AbsorptionIconStyle.GOLD_OUTLINE) {
                    // Golden outline
                    drawTexturedModalRect(left + 8 * i, y, 17, 27, 9, 9, 0xFFFFFF);
                }
                // Shiny glint on top, same as hearts.
                if (!info.hardcoreMode || absorptionIconStyle == AbsorptionIconStyle.SHIELD) {
                    drawTexturedModalRect(left + 8 * i, y, 17, 0, 9, 9, 0xCCFFFFFF);
                }
            }
        }

        GlStateManager.disableBlend();
        mc.textureManager.bindTexture(Gui.ICONS);
    }

    private void drawVanillaHearts(int left, int top) {
        int textureX = info.recentlyHurtHighlight ? 25 : 16;
        int textureY = 9 * (info.hardcoreMode ? 5 : 0);
        int margin = 16;

        float healthMax = Math.min(info.health, 20);
        float absorbRemaining = info.absorption;
        float healthTotal = info.healthInt + info.absorptionInt;

        int iStart = MathHelper.ceil((healthMax + (info.absorptionStyle == AbsorptionIconStyle.VANILLA ? info.absorptionInt : 0)) / 2f) - 1;
        for (int i = iStart; i >= 0; --i) {
            int row = MathHelper.ceil((i + 1) / 10f) - 1;
            int x = left + i % 10 * 8;
            int y = info.offsetHeartPosY(i, top - row * info.rowHeight);

            drawTexturedModalRect(x, y, textureX, textureY, 9, 9);

            if (info.recentlyHurtHighlight) {
                if (i * 2 + 1 < info.previousHealthInt)
                    drawTexturedModalRect(x, y, margin + 54, textureY, 9, 9);
                else if (i * 2 + 1 == info.previousHealthInt)
                    drawTexturedModalRect(x, y, margin + 63, textureY, 9, 9);
            }

            if (absorbRemaining > 0f && info.absorptionStyle == AbsorptionIconStyle.VANILLA) {
                if (MathUtils.doublesEqual(absorbRemaining, info.absorption) && MathUtils.doublesEqual(info.absorption % 2f, 1f)) {
                    drawTexturedModalRect(x, y, margin + 153, textureY, 9, 9);
                    absorbRemaining -= 1f;
                } else {
                    if (i * 2 + 1 < healthTotal)
                        drawTexturedModalRect(x, y, margin + 144, textureY, 9, 9);
                    absorbRemaining -= 2f;
                }
            } else {
                if (i * 2 + 1 < info.healthInt)
                    drawTexturedModalRect(x, y, margin + 36, textureY, 9, 9);
                else if (i * 2 + 1 == info.healthInt)
                    drawTexturedModalRect(x, y, margin + 45, textureY, 9, 9);
            }
        }
    }

    private void renderHealthText(Minecraft mc, float current, float max, int offsetX, int offsetY, HealthTextStyle style, HealthTextColor styleColor) {
        final float scale = style.getScale();
        final int left = (int) ((info.scaledWindowWidth / 2 + offsetX) / scale);
        // GuiIngameForge.left_height == 59 in normal cases. Making it a constant should fix some issues.
        final int top = (int) ((info.scaledWindowHeight + offsetY + (1 / scale)) / scale);

        // Draw health string
        mc.profiler.startSection("shTextPreDraw");
        String healthString = style.textFor(current, max);
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        int stringWidth = fontRenderer.getStringWidth(healthString);
        int color;
        float divisor = max == 0 ? current : max;
        switch (styleColor) {
            case TRANSITION:
//                color = Color.HSBtoRGB(0.34f * current / divisor, 0.7f, 1.0f);
                color = Color.blend(
                        Config.CLIENT.healthTextEmptyColor.get(),
                        Config.CLIENT.healthTextFullColor.get(),
                        current / divisor);
                break;
            case PSYCHEDELIC:
                color = java.awt.Color.HSBtoRGB(
                        (ClientTicks.ticksInGame() % COLOR_CHANGE_PERIOD) / COLOR_CHANGE_PERIOD,
                        0.55f * current / divisor, 1.0f);
                break;
            case SOLID:
            default:
                color = Config.CLIENT.healthTextFullColor.get();
                break;
        }
        mc.profiler.endSection();

        mc.profiler.startSection("shTextDraw");
        GlStateManager.pushMatrix();
        GlStateManager.scalef(scale, scale, 1);
        fontRenderer.drawStringWithShadow(healthString, left - stringWidth - 2, top, color);
        GlStateManager.popMatrix();
        mc.profiler.endSection();
    }

    private void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height, int color) {
        float a = ((color >> 24) & 255) / 255f;
        if (a <= 0f)
            a = 1f;
        float r = ((color >> 16) & 255) / 255f;
        float g = ((color >> 8) & 255) / 255f;
        float b = (color & 255) / 255f;
        GlStateManager.color4f(r, g, b, a);
        drawTexturedModalRect(x, y, textureX, textureY, width, height);
        GlStateManager.color4f(1, 1, 1, 1);
    }

    private static int getColorForRow(int row, boolean absorption) {
        List<Integer> colors = absorption ? Config.CLIENT.absorptionHeartColors.get() : Config.CLIENT.heartColors.get();
        int index = Config.CLIENT.heartColorLooping.get()
                ? row % colors.size()
                : MathUtils.clamp(row, 0, colors.size() - 1);
        return colors.get(index);
    }

    private static boolean showEffectHearts(EntityPlayer player) {
        return player.isPotionActive(MobEffects.POISON) || player.isPotionActive(MobEffects.WITHER);
    }

    private static int effectHeartColor(EntityPlayer player) {
        if (player.isPotionActive(MobEffects.WITHER))
            return 0x663E47;
        if (player.isPotionActive(MobEffects.POISON))
            return 0x4E9331;
        return 0xFFFFFF;
    }
}
