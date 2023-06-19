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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.silentchaos512.lib.event.ClientTicks;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.client.gui.TextureSlice;
import net.silentchaos512.scalinghealth.config.SHConfig;
import net.silentchaos512.utils.Color;
import net.silentchaos512.utils.MathUtils;

import java.util.List;

/**
 * Handles display of regular and absorption hearts.
 * Much of the code can be found in {@link net.minecraftforge.client.gui.overlay.ForgeGui}.
 */
public final class HeartDisplayHandler extends Screen {
    public static final HeartDisplayHandler INSTANCE = new HeartDisplayHandler(Component.empty());

    private static final float COLOR_CHANGE_PERIOD = 150;
    private static final ResourceLocation TEXTURE = new ResourceLocation(ScalingHealth.MOD_ID, "textures/gui/hud.png");

    private static final TextureSlice TANK_SHINE = new TextureSlice(TEXTURE, 44, 0, 5, 5);
    private static final TextureSlice TANK_OUTLINE = new TextureSlice(TEXTURE,44, 5, 5, 5);
    private static final TextureSlice TANK_FULL = new TextureSlice(TEXTURE, 44, 10, 5, 5);
    private static final TextureSlice TANK_EMPTY = new TextureSlice(TEXTURE, 44, 15, 5, 5);

    private final HeartsInfo info = new HeartsInfo();

    private HeartDisplayHandler(Component title) {
        super(title);
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onHealthBar(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.PLAYER_HEALTH.type()) return;
        
        if (info.heartStyle.get() == HeartIconStyle.VANILLA) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        // Health text
        if (mc.gameMode.getPlayerMode().isSurvival()) {
            // Draw health string?
            if (SHConfig.CLIENT.healthTextStyle.get() != HealthTextStyle.DISABLED) {
                mc.getProfiler().push("scalinghealthRenderHealthText");
                renderHealthText(mc, event.getGuiGraphics(), info.health, info.maxHealth,
                        -91 + SHConfig.CLIENT.healthTextOffsetX.get(),
                        -38 + SHConfig.CLIENT.healthTextOffsetY.get(),
                        SHConfig.CLIENT.healthTextStyle.get(),
                        SHConfig.CLIENT.healthTextColorStyle.get());
                mc.getProfiler().pop();
            }
            // Draw absorption amount string?
            if (SHConfig.CLIENT.absorptionTextStyle.get() != HealthTextStyle.DISABLED && player.getAbsorptionAmount() > 0) {
                mc.getProfiler().push("scalinghealthRenderAbsorptionText");
                renderHealthText(mc, event.getGuiGraphics(), player.getAbsorptionAmount(), 0,
                        -91 + SHConfig.CLIENT.absorptionTextOffsetX.get(),
                        -49 + SHConfig.CLIENT.absorptionTextOffsetY.get(),
                        SHConfig.CLIENT.absorptionTextStyle.get(),
                        HealthTextColor.SOLID);
                mc.getProfiler().pop();
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onHealthDraw(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != VanillaGuiOverlay.PLAYER_HEALTH.type() ||
                info.heartStyle.get() == HeartIconStyle.VANILLA ||
                Minecraft.getInstance().options.hideGui ||
                !getGui().shouldDrawSurvivalElements()
        )
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        event.setCanceled(true);
        mc.getProfiler().push("scalinghealthRenderHearts");
        renderHearts(event, mc, player);
        mc.getProfiler().pop();
    }

    private void renderHearts(RenderGuiOverlayEvent event, Minecraft mc, Player player) {
        GuiGraphics graphics = event.getGuiGraphics();
        info.update();

        RenderSystem.enableBlend();

        float absorb = Mth.ceil(player.getAbsorptionAmount());

        final int left = info.scaledWindowWidth / 2 - 91;
        int top = info.scaledWindowHeight - getGui().leftHeight;
        getGui().leftHeight += info.rowsUsedInHud * info.rowHeight;
        if (info.rowHeight != 10)
            getGui().leftHeight += 10 - info.rowHeight;

        // Draw vanilla hearts
        drawVanillaHearts(graphics, left, top);

        int potionOffset = info.hardcoreMode ? 27 : 0;

        // Draw extra hearts (only top 2 rows)
        RenderSystem.setShaderTexture(0, TEXTURE);
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
                blitWithColor(graphics,left + 8 * j, y, 0, potionOffset, 9, 9, rowColor);
            }
            boolean anythingDrawn = j > 0;

            // Half heart on the end?
            if (info.healthInt % 2 == 1 && renderHearts < 10) {
                int y = info.offsetHeartPosY(j, top);
                blitWithColor(graphics,left + 8 * renderHearts, y, 9, potionOffset, 9, 9, rowColor);
                anythingDrawn = true;
            }

            // Outline for last heart, to make seeing max health a little easier.
            if (SHConfig.CLIENT.lastHeartOutline.get() && anythingDrawn && row == maxHealthRows - 1) {
                // Get position of last partial/full heart
                j = (int) (Math.ceil(player.getMaxHealth() % 20f / 2f)) - 1;
                if (j < 0) j += 10;
                int y = info.offsetHeartPosY(j, top);
                int color = SHConfig.CLIENT.lastHeartOutlineColor.get();
                blitWithColor(graphics,left + 8 * j, y, 17, 9, 9, 9, color);
            }
        }

        for (int i = 0; i < 10 && i < Math.ceil(info.healthInt / 2f); ++i) {
            int y = info.offsetHeartPosY(i, top);
            // Effect hearts (poison, wither)
            if (showEffectHearts(player)) {
                int color = effectHeartColor(player);
                blitWithColor(graphics,left + 8 * i, y, 0, 54, 9, 9, color);
            }
            // Shiny glint on top of the hearts, a single white pixel in the upper left <3
            if (!info.hardcoreMode) {
                blitWithColor(graphics,left + 8 * i, y, 17, 0, 9, 9, 0xCCFFFFFF);
            }
        }

        // Tanks

        if (info.getMaxHeartTanks() > 0 && SHConfig.CLIENT.heartTanks.get()) {
            int tankRows = info.getHeartTankRowCount();
            int maxTankRows = info.getMaxHeartTankRowCount();

            for (int row = 0; row < maxTankRows; ++row) {
                int filledTanksInRow = info.getFilledHeartTanksInRow(row);
                int allTanksInRow = info.getAllHeartTanksInRow(row);
                int rowColor = getColorForRow(row, false);
                top -= 4;
                getGui().leftHeight += 4;

                // Draw tanks
                int x;
                for (x = 0; x < allTanksInRow; ++x) {
                    if (x < filledTanksInRow) {
                        TANK_FULL.blit(graphics, left + 4 * x, top, rowColor);
                    } else {
                        TANK_EMPTY.blit(graphics, left + 4 * x, top, 0xFFFFFF);
                    }
                }
                boolean anythingDrawn = x > 0;

                if (SHConfig.CLIENT.lastHeartOutline.get() && anythingDrawn && row == maxTankRows - 1) {
                    x = (int) (Math.ceil(allTanksInRow)) - 1;
                    if (x < 0) x += 20;
                    TANK_OUTLINE.blit(graphics, left + 4 * x, top, SHConfig.CLIENT.lastHeartOutlineColor.get());
                }
            }
        }

        // ==========================
        // Absorption hearts override
        // ==========================

        AbsorptionIconStyle absorptionIconStyle = SHConfig.CLIENT.absorptionIconStyle.get();
        if (absorptionIconStyle != AbsorptionIconStyle.VANILLA) {
            int absorbCeil = (int) Math.ceil(absorb);
            rowCount = (int) Math.ceil(absorb / 20);

            // Dark underlay for first row
            int texX = 17;
            int texY = absorptionIconStyle == AbsorptionIconStyle.SHIELD ? 45 : 54;
            for (int i = 0; i < 10 && i < absorb / 2; ++i) {
                int y = info.offsetAbsorptionPosY(i, top);
                blitWithColor(graphics,left + 8 * i, y, texX, texY, 9, 9, 0xFFFFFF);
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
                    blitWithColor(graphics,left + 8 * x, y, texX, texY, 9, 9, rowColor);
                }
                anythingDrawn = x > 0;

                // Half heart on the end?
                if (absorbCeil % 2 == 1 && renderHearts < 10) {
                    int y = info.offsetAbsorptionPosY(x, top);
                    blitWithColor(graphics,left + 8 * renderHearts, y, texX + 9, texY, 9, 9, rowColor);
                    anythingDrawn = true;
                }
            }

            // Add extra bits like outlines on top
            for (int i = 0; i < 10 && i < absorb / 2; ++i) {
                int y = info.offsetAbsorptionPosY(i, top);
                if (absorptionIconStyle == AbsorptionIconStyle.SHIELD) {
                    // Golden hearts in center (shield style only)
                    blitWithColor(graphics,left + 8 * i, y, 17, 36, 9, 9, 0xFFFFFF);
                } else if (absorptionIconStyle == AbsorptionIconStyle.GOLD_OUTLINE) {
                    // Golden outline
                    blitWithColor(graphics,left + 8 * i, y, 17, 27, 9, 9, 0xFFFFFF);
                }
                // Shiny glint on top, same as hearts.
                if (!info.hardcoreMode || absorptionIconStyle == AbsorptionIconStyle.SHIELD) {
                    blitWithColor(graphics,left + 8 * i, y, 17, 0, 9, 9, 0xCCFFFFFF);
                }
            }
        }
    }

    private void drawVanillaHearts(GuiGraphics graphics, int left, int top) {
        int textureX = info.recentlyHurtHighlight ? 25 : 16;
        int textureY = 9 * (info.hardcoreMode ? 5 : 0);
        int margin = 16;

        float healthMax = Math.min(info.maxHealth, 20);
        float absorbRemaining = info.absorption;
        float healthTotal = info.healthInt + info.absorptionInt;

        int iStart = Mth.ceil((healthMax + (info.absorptionStyle.get() == AbsorptionIconStyle.VANILLA ? info.absorptionInt : 0)) / 2f) - 1;
        for (int i = iStart; i >= 0; --i) {
            int row = Mth.ceil((i + 1) / 10f) - 1;
            int x = left + i % 10 * 8;
            int y = info.offsetHeartPosY(i, top - row * info.rowHeight);

            graphics.blit(TEXTURE, x, y, textureX, textureY, 9, 9);

            if (info.recentlyHurtHighlight) {
                if (i * 2 + 1 < info.previousHealthInt)
                    graphics.blit(TEXTURE, x, y, margin + 54, textureY, 9, 9);
                else if (i * 2 + 1 == info.previousHealthInt)
                    graphics.blit(TEXTURE, x, y, margin + 63, textureY, 9, 9);
            }

            if (absorbRemaining > 0f && info.absorptionStyle.get() == AbsorptionIconStyle.VANILLA) {
                if (MathUtils.doublesEqual(absorbRemaining, info.absorption) && MathUtils.doublesEqual(info.absorption % 2f, 1f)) {
                    graphics.blit(TEXTURE, x, y, margin + 153, textureY, 9, 9);
                    absorbRemaining -= 1f;
                } else {
                    if (i * 2 + 1 < healthTotal)
                        graphics.blit(TEXTURE, x, y, margin + 144, textureY, 9, 9);
                    absorbRemaining -= 2f;
                }
            } else {
                if (i * 2 + 1 < info.healthInt)
                    graphics.blit(TEXTURE,x, y, margin + 36, textureY, 9, 9);
                else if (i * 2 + 1 == info.healthInt)
                    graphics.blit(TEXTURE,x, y, margin + 45, textureY, 9, 9);
            }
        }
    }

    private void renderHealthText(Minecraft mc, GuiGraphics graphics, float current, float max, int offsetX, int offsetY, HealthTextStyle style, HealthTextColor styleColor) {
        final float scale = (float) style.getScale();
        final int left = (int) ((info.scaledWindowWidth / 2 + offsetX) / scale);
        // GuiIngameForge.leftHeight == 59 in normal cases. Making it a constant should fix some issues.
        final int top = (int) ((info.scaledWindowHeight + offsetY + (1 / scale)) / scale);

        // Draw health string
        mc.getProfiler().push("shTextPreDraw");
        String healthString = style.textFor(current, max);
        Font fontRenderer = Minecraft.getInstance().font;
        int stringWidth = fontRenderer.width(healthString);
        int color;
        float divisor = max == 0 ? current : max;
        switch (styleColor) {
            case TRANSITION:
//                color = Color.HSBtoRGB(0.34f * current / divisor, 0.7f, 1.0f);
                color = Color.blend(
                        SHConfig.CLIENT.healthTextEmptyColor.get(),
                        SHConfig.CLIENT.healthTextFullColor.get(),
                        current / divisor);
                break;
            case PSYCHEDELIC:
                color = java.awt.Color.HSBtoRGB(
                        (ClientTicks.ticksInGame() % COLOR_CHANGE_PERIOD) / COLOR_CHANGE_PERIOD,
                        0.55f * current / divisor, 1.0f);
                break;
            case SOLID:
            default:
                color = SHConfig.CLIENT.healthTextFullColor.get();
                break;
        }
        mc.getProfiler().pop();

        mc.getProfiler().push("shTextDraw");
        PoseStack stack = graphics.pose();
        stack.pushPose();
        stack.scale(scale, scale, 1);
        graphics.drawString(font, healthString, left - stringWidth - 2, top, color);
        stack.popPose();
        mc.getProfiler().pop();
    }

    private void blitWithColor(GuiGraphics graphics, int x, int y, int textureX, int textureY, int width, int height, int color) {
        float a = ((color >> 24) & 255) / 255f;
        if (a <= 0f)
            a = 1f;
        float r = ((color >> 16) & 255) / 255f;
        float g = ((color >> 8) & 255) / 255f;
        float b = (color & 255) / 255f;
        graphics.setColor(r, g, b, a);
        graphics.blit(TEXTURE, x, y, textureX, textureY, width, height);
        graphics.setColor(1, 1, 1, 1);
    }

    private static int getColorForRow(int row, boolean absorption) {
        List<Integer> colors = absorption ? SHConfig.CLIENT.absorptionHeartColors.get() : SHConfig.CLIENT.heartColors.get();
        int index = SHConfig.CLIENT.heartColorLooping.get()
                ? row % colors.size()
                : MathUtils.clamp(row, 0, colors.size() - 1);
        return colors.get(index);
    }

    private static boolean showEffectHearts(Player player) {
        return player.hasEffect(MobEffects.POISON) || player.hasEffect(MobEffects.WITHER);
    }

    private static int effectHeartColor(Player player) {
        if (player.hasEffect(MobEffects.WITHER))
            return 0x663E47;
        if (player.hasEffect(MobEffects.POISON))
            return 0x4E9331;
        return 0xFFFFFF;
    }

    public static ForgeGui getGui() {
        return (ForgeGui) Minecraft.getInstance().gui;
    }
}
