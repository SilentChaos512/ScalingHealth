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
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
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
import java.util.Random;

/**
 * Handles display of regular and absorption hearts.
 * <p>For future reference, much of the vanilla code can be found in {@link GuiIngameForge}.</p>
 */
public final class HeartDisplayHandler extends Gui {
    public static final HeartDisplayHandler INSTANCE = new HeartDisplayHandler();

    private static final float COLOR_CHANGE_PERIOD = 150;
    private static final ResourceLocation TEXTURE = new ResourceLocation(ScalingHealth.MOD_ID, "textures/gui/hud.png");

    private long lastSystemTime = 0;
    private int playerHealth = 0;
    private int lastPlayerHealth = 0;
    private final Random rand = new Random();

    private HeartDisplayHandler() {}

    @SubscribeEvent(receiveCanceled = true)
    public void onHealthBar(RenderGameOverlayEvent.Pre event) {
        if (Config.CLIENT.heartIconStyle.get() == HeartIconStyle.VANILLA) return;

        Minecraft mc = Minecraft.getInstance();
        EntityPlayer player = mc.player;

        // Health text
        if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT && mc.playerController.gameIsSurvivalOrAdventure()) {
            // Draw health string?
            if (Config.CLIENT.healthTextStyle.get() != HealthTextStyle.DISABLED) {
                renderHealthText(mc, player.getHealth(), player.getMaxHealth(),
                        -91 + Config.CLIENT.healthTextOffsetX.get(),
                        -38 + Config.CLIENT.healthTextOffsetY.get(),
                        Config.CLIENT.healthTextStyle.get(),
                        Config.CLIENT.healthTextColorStyle.get());
            }
            // Draw absorption amount string?
            if (Config.CLIENT.absorptionTextStyle.get() != HealthTextStyle.DISABLED && player.getAbsorptionAmount() > 0) {
                renderHealthText(mc, player.getAbsorptionAmount(), 0,
                        -91 + Config.CLIENT.absorptionTextOffsetX.get(),
                        -49 + Config.CLIENT.absorptionTextOffsetY.get(),
                        Config.CLIENT.absorptionTextStyle.get(),
                        HealthTextColor.SOLID);
            }
        }

        // Hearts
        if (event.getType() == RenderGameOverlayEvent.ElementType.HEALTH && Config.CLIENT.heartIconStyle.get() != HeartIconStyle.VANILLA) {
            event.setCanceled(true);
            renderHearts(event, mc, player);
        }
    }

    private void renderHearts(RenderGameOverlayEvent event, Minecraft mc, EntityPlayer player) {
        final boolean hardcoreMode = mc.world.getWorldInfo().isHardcore();

        int width = mc.mainWindow.getScaledWidth();
        int height = mc.mainWindow.getScaledHeight();
        GlStateManager.enableBlend();

        int health = MathHelper.ceil(player.getHealth());
        boolean highlight = player.hurtResistantTime / 3 % 2 == 1;
        int updateCounter = ClientTicks.ticksInGame();

        if (health < playerHealth && player.hurtResistantTime > 0 || health > playerHealth && player.hurtResistantTime > 0) {
            lastSystemTime = Util.milliTime();
        }

        if (Util.milliTime() - lastSystemTime > 1000) {
            playerHealth = health;
            lastPlayerHealth = health;
            lastSystemTime = Util.milliTime();
        }

        playerHealth = health;
        int healthLast = lastPlayerHealth;

        IAttributeInstance attrMaxHealth = player.getAttribute(SharedMonsterAttributes.MAX_HEALTH);
        float healthMax = Math.min((float) attrMaxHealth.getValue(), 20);
        float absorb = MathHelper.ceil(player.getAbsorptionAmount());

        int healthRows = absorb > 0 ? 2 : 1; // MathHelper.ceil((healthMax + absorb) / 2f / 10f);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        rand.setSeed(updateCounter * 312871);
        int[] lowHealthBob = new int[10];
        for (int i = 0; i < lowHealthBob.length; ++i) lowHealthBob[i] = rand.nextInt(2);

        final int left = width / 2 - 91;
        final int top = height - GuiIngameForge.left_height;
        GuiIngameForge.left_height += healthRows * rowHeight;
        if (rowHeight != 10)
            GuiIngameForge.left_height += 10 - rowHeight;

        int regen = -1;
        if (player.isPotionActive(MobEffects.REGENERATION))
            regen = updateCounter % 25;

        final int TOP = 9 * (hardcoreMode ? 5 : 0);
        final int BACKGROUND = (highlight ? 25 : 16);
        int MARGIN = 16;
//        if (player.isPotionActive(MobEffects.POISON))
//            MARGIN += 36;
//        else if (player.isPotionActive(MobEffects.WITHER))
//            MARGIN += 72;

        // Draw vanilla hearts
        drawVanillaHearts(health, highlight, healthLast, healthMax, rowHeight, left, top, regen, lowHealthBob, TOP, BACKGROUND, MARGIN);

//        int potionOffset = player.isPotionActive(MobEffects.WITHER) ? 18
//                : (player.isPotionActive(MobEffects.POISON) ? 9 : 0) + (hardcoreMode ? 27 : 0);
        int potionOffset = hardcoreMode ? 27 : 0;

        // Draw extra hearts (only top 2 rows)
        mc.textureManager.bindTexture(TEXTURE);
        health = MathHelper.ceil(player.getHealth());
        int rowCount = getCustomHeartRowCount(health);
        int maxHealthRows = getCustomHeartRowCount((int) player.getMaxHealth());
        final boolean healthIsLow = health <= healthMax / 5;

        for (int row = Math.max(0, rowCount - 2); row < rowCount; ++row) {
            int actualRow = row + (Config.CLIENT.heartIconStyle.get() == HeartIconStyle.REPLACE_ALL ? 0 : 1);
            int renderHearts = Math.min((health - 20 * actualRow) / 2, 10);
            int rowColor = getColorForRow(row, false);

            // Draw the hearts
            int j;
            int y;
            for (j = 0; j < renderHearts; ++j) {
                y = top + (j == regen ? -2 : 0);
                if (healthIsLow)
                    y += lowHealthBob[MathHelper.clamp(j, 0, lowHealthBob.length - 1)];
                drawTexturedModalRect(left + 8 * j, y, 0, potionOffset, 9, 9, rowColor);
            }
            boolean anythingDrawn = j > 0;

            // Half heart on the end?
            if (health % 2 == 1 && renderHearts < 10) {
                y = top + (j == regen ? -2 : 0);
                if (healthIsLow)
                    y += lowHealthBob[MathHelper.clamp(j, 0, lowHealthBob.length - 1)];
                drawTexturedModalRect(left + 8 * renderHearts, y, 9, potionOffset, 9, 9, rowColor);
                anythingDrawn = true;
            }

            // Outline for last heart, to make seeing max health a little easier.
            if (Config.CLIENT.lastHeartOutline.get() && anythingDrawn && row == maxHealthRows - 1) {
                // Get position of last partial/full heart
                j = (int) (Math.ceil(player.getMaxHealth() % 20f / 2f)) - 1;
                if (j < 0) j += 10;
                y = top + (j == regen ? -2 : 0);
                if (healthIsLow)
                    y += lowHealthBob[MathHelper.clamp(j, 0, lowHealthBob.length - 1)];
                int color = Config.CLIENT.lastHeartOutlineColor.get().getColor();
                drawTexturedModalRect(left + 8 * j, y, 17, 9, 9, 9, color);
            }
        }

        for (int i = 0; i < 10 && i < Math.ceil(health / 2f); ++i) {
            int y = top + (i == regen ? -2 : 0);
            if (healthIsLow)
                y += lowHealthBob[MathHelper.clamp(i, 0, lowHealthBob.length - 1)];
            // Effect hearts (poison, wither)
            if (showEffectHearts(player)) {
                int color = effectHeartColor(player);
                drawTexturedModalRect(left + 8 * i, y, 0, 54, 9, 9, color);
            }
            // Shiny glint on top of the hearts, a single white pixel in the upper left <3
            if (!hardcoreMode) {
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
                int y = top - 10 + (i == regen - 10 ? -2 : 0);
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
                int y;
                for (x = 0; x < renderHearts; ++x) {
                    y = top - 10 + (x == regen - 10 ? -2 : 0);
                    drawTexturedModalRect(left + 8 * x, y, texX, texY, 9, 9, rowColor);
                }
                anythingDrawn = x > 0;

                // Half heart on the end?
                if (absorbCeil % 2 == 1 && renderHearts < 10) {
                    y = top - 10 + (x == regen - 10 ? -2 : 0);
                    drawTexturedModalRect(left + 8 * renderHearts, y, texX + 9, texY, 9, 9, rowColor);
                    anythingDrawn = true;
                }
            }

            // Add extra bits like outlines on top
            for (int i = 0; i < 10 && i < absorb / 2; ++i) {
                int y = top - 10 + (i == regen - 10 ? -2 : 0);
                if (absorptionIconStyle == AbsorptionIconStyle.SHIELD) {
                    // Golden hearts in center (shield style only)
                    drawTexturedModalRect(left + 8 * i, y, 17, 36, 9, 9, 0xFFFFFF);
                } else if (absorptionIconStyle == AbsorptionIconStyle.GOLD_OUTLINE) {
                    // Golden outline
                    drawTexturedModalRect(left + 8 * i, y, 17, 27, 9, 9, 0xFFFFFF);
                }
                // Shiny glint on top, same as hearts.
                if (!hardcoreMode || absorptionIconStyle == AbsorptionIconStyle.SHIELD) {
                    drawTexturedModalRect(left + 8 * i, y, 17, 0, 9, 9, 0xCCFFFFFF);
                }
            }
        }

        GlStateManager.disableBlend();
        mc.textureManager.bindTexture(Gui.ICONS);
    }

    private static int getCustomHeartRowCount(int health) {
        return Config.CLIENT.heartIconStyle.get() == HeartIconStyle.REPLACE_ALL
                ? MathHelper.ceil(health / 20f)
                : health / 20;
    }

    private void drawVanillaHearts(int health, boolean highlight, int healthLast, float healthMax, int rowHeight, int left, int top, int regen, int[] lowHealthBob, int TOP, int BACKGROUND, int MARGIN) {
        float absorb = MathHelper.ceil(Minecraft.getInstance().player.getAbsorptionAmount());
        float absorbRemaining = absorb;
        float healthTotal = health + absorb;

        AbsorptionIconStyle absorptionIconStyle = Config.CLIENT.absorptionIconStyle.get();
        int iStart = MathHelper.ceil((healthMax + (absorptionIconStyle == AbsorptionIconStyle.VANILLA ? absorb : 0)) / 2f) - 1;
        for (int i = iStart; i >= 0; --i) {
            int row = MathHelper.ceil((i + 1) / 10f) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;

            if (health <= 4)
                y += lowHealthBob[MathHelper.clamp(i, 0, lowHealthBob.length - 1)];
            if (i == regen)
                y -= 2;

            drawTexturedModalRect(x, y, BACKGROUND, TOP, 9, 9);

            if (highlight) {
                if (i * 2 + 1 < healthLast)
                    drawTexturedModalRect(x, y, MARGIN + 54, TOP, 9, 9);
                else if (i * 2 + 1 == healthLast)
                    drawTexturedModalRect(x, y, MARGIN + 63, TOP, 9, 9);
            }

            if (absorbRemaining > 0f && absorptionIconStyle == AbsorptionIconStyle.VANILLA) {
                if (MathUtils.doublesEqual(absorbRemaining, absorb) && MathUtils.doublesEqual(absorb % 2f, 1f)) {
                    drawTexturedModalRect(x, y, MARGIN + 153, TOP, 9, 9);
                    absorbRemaining -= 1f;
                } else {
                    if (i * 2 + 1 < healthTotal)
                        drawTexturedModalRect(x, y, MARGIN + 144, TOP, 9, 9);
                    absorbRemaining -= 2f;
                }
            } else {
                if (i * 2 + 1 < health)
                    drawTexturedModalRect(x, y, MARGIN + 36, TOP, 9, 9);
                else if (i * 2 + 1 == health)
                    drawTexturedModalRect(x, y, MARGIN + 45, TOP, 9, 9);
            }
        }
    }

    private static void renderHealthText(Minecraft mc, float current, float max, int offsetX, int offsetY, HealthTextStyle style, HealthTextColor styleColor) {
        final float scale = style.getScale();
        final int width = mc.mainWindow.getScaledWidth();
        final int height = mc.mainWindow.getScaledHeight();
        final int left = (int) ((width / 2 + offsetX) / scale);
        // GuiIngameForge.left_height == 59 in normal cases. Making it a constant should fix some issues.
        final int top = (int) ((height + offsetY + (1 / scale)) / scale);

        // Draw health string
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
                        current / divisor
                ).getColor();
                break;
            case PSYCHEDELIC:
                color = java.awt.Color.HSBtoRGB(
                        (ClientTicks.ticksInGame() % COLOR_CHANGE_PERIOD) / COLOR_CHANGE_PERIOD,
                        0.55f * current / divisor, 1.0f);
                break;
            case SOLID:
            default:
                color = Config.CLIENT.healthTextFullColor.get().getColor();
                break;
        }
        GlStateManager.pushMatrix();
        GlStateManager.scalef(scale, scale, 1);
        fontRenderer.drawStringWithShadow(healthString, left - stringWidth - 2, top, color);
        GlStateManager.popMatrix();
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
        List<Color> colors = absorption ? Config.CLIENT.absorptionHeartColors.get() : Config.CLIENT.heartColors.get();
        return colors.get(row % colors.size()).getColor();
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
