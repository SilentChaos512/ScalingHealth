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
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.silentchaos512.lib.config.ConfigBase;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;

import java.awt.*;
import java.util.Random;

public class HeartDisplayHandler extends Gui {
    public enum TextStyle {
        DISABLED, ROWS, HEALTH_AND_MAX;

        private static final String COMMENT = "Determines what the text next to your hearts will display. DISABLED will display nothing, ROWS will display the number of remaining rows that have health left, and HEALTH_AND_MAX will display your actual health and max health values.";

        public static TextStyle loadFromConfig(ConfigBase config) {
            return config.loadEnum("Health Text Style", Config.CAT_CLIENT, TextStyle.class, ROWS, COMMENT);
        }

    }

    public enum TextColor {
        GREEN_TO_RED, WHITE, PSYCHEDELIC;

        public static final String COMMENT = "Determines the color of the text next to your hearts. GREEN_TO_RED displays green at full health, and moves to red as you lose health. WHITE will just be good old fashioned white text. Set to PSYCHEDELIC if you want to taste the rainbow.";

        public static TextColor loadFromConfig(ConfigBase config) {
            return config.loadEnum("Health Text Color", Config.CAT_CLIENT, TextColor.class, GREEN_TO_RED, COMMENT);
        }

    }

    public static final HeartDisplayHandler INSTANCE = new HeartDisplayHandler();

    private static final float COLOR_CHANGE_PERIOD = 150;
    private static final ResourceLocation TEXTURE = new ResourceLocation(ScalingHealth.MOD_ID_LOWER, "textures/gui/hud.png");

    private long lastSystemTime = 0;
    private int playerHealth = 0;
    private int lastPlayerHealth = 0;
    private Random rand = new Random();

    private HeartDisplayHandler() {
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onHealthBar(RenderGameOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;

        // Health text
        TextStyle style = Config.HEART_DISPLAY_TEXT_STYLE;
        TextColor styleColor = Config.HEART_DISPLAY_TEXT_COLOR;
        if (event.getType() == ElementType.TEXT && style != TextStyle.DISABLED && mc.playerController.gameIsSurvivalOrAdventure()) {
            renderHealthText(event, player, style, styleColor);
        }

        // Hearts
        if (event.getType() == ElementType.HEALTH && Config.CHANGE_HEART_RENDERING) {
            event.setCanceled(true);
            renderHearts(event, mc, player);
        }
    }

    private void renderHearts(RenderGameOverlayEvent event, Minecraft mc, EntityPlayer player) {
        final boolean hardcoreMode = mc.world.getWorldInfo().isHardcoreModeEnabled();

        int width = event.getResolution().getScaledWidth();
        int height = event.getResolution().getScaledHeight();
        GlStateManager.enableBlend();

        int health = MathHelper.ceil(player.getHealth());
        boolean highlight = player.hurtResistantTime / 3 % 2 == 1;
        int updateCounter = ClientTickHandler.ticksInGame;

        if (health < playerHealth && player.hurtResistantTime > 0 || health > playerHealth && player.hurtResistantTime > 0) {
            lastSystemTime = Minecraft.getSystemTime();
        }

        if (Minecraft.getSystemTime() - lastSystemTime > 1000) {
            playerHealth = health;
            lastPlayerHealth = health;
            lastSystemTime = Minecraft.getSystemTime();
        }

        playerHealth = health;
        int healthLast = lastPlayerHealth;

        IAttributeInstance attrMaxHealth = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        float healthMax = Math.min((float) attrMaxHealth.getAttributeValue(), 20);
        float absorb = MathHelper.ceil(player.getAbsorptionAmount());

        int healthRows = absorb > 0 ? 2 : 1; // MathHelper.ceil((healthMax + absorb) / 2f / 10f);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        rand.setSeed(updateCounter * 312871);

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
        drawVanillaHearts(health, highlight, healthLast, healthMax, rowHeight, left, top, regen, TOP, BACKGROUND, MARGIN);

//        int potionOffset = player.isPotionActive(MobEffects.WITHER) ? 18
//                : (player.isPotionActive(MobEffects.POISON) ? 9 : 0) + (hardcoreMode ? 27 : 0);
        int potionOffset = hardcoreMode ? 27 : 0;

        // Draw extra hearts (only top 2 rows)
        mc.renderEngine.bindTexture(TEXTURE);
        health = MathHelper.ceil(player.getHealth());
        int rowCount = getCustomHeartRowCount(health);

        for (int row = Math.max(0, rowCount - 2); row < rowCount; ++row) {
            int actualRow = row + (Config.REPLACE_VANILLA_HEARTS_WITH_CUSTOM ? 0 : 1);
            int renderHearts = Math.min((health - 20 * actualRow) / 2, 10);
            int rowColor = getColorForRow(row);
            boolean anythingDrawn;

            // Draw the hearts
            int j;
            int y;
            for (j = 0; j < renderHearts; ++j) {
                y = top + (j == regen ? -2 : 0);
                drawTexturedModalRect(left + 8 * j, y, 0, potionOffset, 9, 9, rowColor);
            }
            anythingDrawn = j > 0;

            // Half heart on the end?
            if (health % 2 == 1 && renderHearts < 10) {
                drawTexturedModalRect(left + 8 * renderHearts, top, 9, potionOffset, 9, 9, rowColor);
                anythingDrawn = true;
            }

            // Outline for last heart, to make seeing max health a little easier.
            if (Config.LAST_HEART_OUTLINE_ENABLED && anythingDrawn && row == rowCount - 1) {
                // Get position of last partial/full heart
                j = (int) (Math.ceil(player.getMaxHealth() % 20f / 2f)) - 1;
                if (j < 0) j += 10;
                y = top + (j == regen ? -2 : 0);
                drawTexturedModalRect(left + 8 * j, y, 17, 9, 9, 9, Config.LAST_HEART_OUTLINE_COLOR);
            }
        }

        for (int i = 0; i < 10 && i < Math.ceil(health / 2f); ++i) {
            int y = top + (i == regen ? -2 : 0);
            // Effect hearts (poison, wither)
            if (showEffectHearts(player)) {
                int color = effectHeartColor(player);
                drawTexturedModalRect(left + 8 * i, y, 0, 54, 9, 9, color);
            }
            // Shiny glint on top of the hearts, a single white pixel in the upper left <3
            drawTexturedModalRect(left + 8 * i, y, 17, potionOffset, 9, 9, 0xAAFFFFFF);
        }

        // Absorption hearts override
        int absorbCeil = (int) Math.ceil(absorb);
        rowCount = (int) Math.ceil(absorb / 20);

        // Dark underlay for first row
        for (int i = 0; i < 10 && i < absorb / 2; ++i) {
            int y = top - 10 + (i == regen - 10 ? -2 : 0);
            drawTexturedModalRect(left + 8 * i, y, 17, 45, 9, 9, 0xFFFFFF);
        }

        // Draw the top two absorption rows
        for (int i = Math.max(0, rowCount - 2); i < rowCount; ++i) {
            int renderHearts = Math.min((absorbCeil - 20 * i) / 2, 10);
            int rowColor = getColorForRow(i);
            boolean anythingDrawn;

            // Draw the hearts
            int x;
            int y;
            for (x = 0; x < renderHearts; ++x) {
                y = top - 10 + (x == regen - 10 ? -2 : 0);
                drawTexturedModalRect(left + 8 * x, y, 26, potionOffset, 9, 9, rowColor);
            }
            anythingDrawn = x > 0;

            // Half heart on the end?
            if (absorbCeil % 2 == 1 && renderHearts < 10) {
                drawTexturedModalRect(left + 8 * renderHearts, top - 10, 26 + 9, potionOffset, 9, 9, rowColor);
                anythingDrawn = true;
            }

            // Outline for last heart, to make seeing max health a little easier.
//            if (Config.LAST_HEART_OUTLINE_ENABLED && anythingDrawn && i == MathHelper.ceil(player.getMaxHealth() / 20) - 2) {
//                j = (int) ((player.getMaxHealth() % 20) / 2) - 1;
//                if (j < 0) {
//                    j += 10;
//                }
//                drawTexturedModalRect(left + 8 * j, top + y, 17, 9, 10, 9, Config.LAST_HEART_OUTLINE_COLOR);
//            }
        }

        // Golden hearts in center
        for (int i = 0; i < 10 && i < absorb / 2; ++i) {
            int y = top - 10 + (i == regen - 10 ? -2 : 0);
            drawTexturedModalRect(left + 8 * i, y, 17, 36, 9, 9, 0xFFFFFF);
        }

        GlStateManager.disableBlend();
        mc.renderEngine.bindTexture(Gui.ICONS);
    }

    private int getCustomHeartRowCount(int health) {
        return Config.REPLACE_VANILLA_HEARTS_WITH_CUSTOM ? MathHelper.ceil(health / 20f) : health / 20;
    }

    private void drawVanillaHearts(int health, boolean highlight, int healthLast, float healthMax, int rowHeight, int left, int top, int regen, int TOP, int BACKGROUND, int MARGIN) {
        for (int i = MathHelper.ceil((healthMax /*+ absorb*/) / 2f) - 1; i >= 0; --i) {
            int row = MathHelper.ceil((i + 1) / 10f) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;

            if (health <= 4)
                y += rand.nextInt(2);
            if (i == regen)
                y -= 2;

            drawTexturedModalRect(x, y, BACKGROUND, TOP, 9, 9);

            if (highlight) {
                if (i * 2 + 1 < healthLast)
                    drawTexturedModalRect(x, y, MARGIN + 54, TOP, 9, 9);
                else if (i * 2 + 1 == healthLast)
                    drawTexturedModalRect(x, y, MARGIN + 63, TOP, 9, 9);
            }

//            if (absorbRemaining > 0f) {
//                if (absorbRemaining == absorb && absorb % 2f == 1f) {
//                    drawTexturedModalRect(x, y, MARGIN + 153, TOP, 9, 9);
//                    absorbRemaining -= 1f;
//                } else {
//                    if (i * 2 + 1 < health)
//                        drawTexturedModalRect(x, y, MARGIN + 144, TOP, 9, 9);
//                    absorbRemaining -= 2f;
//                }
//            } else {
            if (i * 2 + 1 < health)
                drawTexturedModalRect(x, y, MARGIN + 36, TOP, 9, 9);
            else if (i * 2 + 1 == health)
                drawTexturedModalRect(x, y, MARGIN + 45, TOP, 9, 9);
//            }
        }
    }

    private void renderHealthText(RenderGameOverlayEvent event, EntityPlayer player, TextStyle style, TextColor styleColor) {
        final float scale = style == TextStyle.ROWS ? 0.65f : 0.5f;
        final int width = event.getResolution().getScaledWidth();
        final int height = event.getResolution().getScaledHeight();
        final int left = (int) ((width / 2 - 91) / scale);
        // GuiIngameForge.left_height == 59 in normal cases. Making it a constant should fix some issues.
        final int top = (int) ((height - 59 + 21 + (1 / scale)) / scale);

        // Draw health string
        String healthString = style == TextStyle.HEALTH_AND_MAX
                ? (int) player.getHealth() + "/" + (int) player.getMaxHealth()
                : (int) Math.ceil(player.getHealth() / 20) + "x";
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int stringWidth = fontRenderer.getStringWidth(healthString);
        int color;
        switch (styleColor) {
            case GREEN_TO_RED:
                color = Color.HSBtoRGB(0.34f * playerHealth / player.getMaxHealth(), 0.7f, 1.0f);
                break;
            case PSYCHEDELIC:
                color = Color.HSBtoRGB(
                        (ClientTickHandler.ticksInGame % COLOR_CHANGE_PERIOD) / COLOR_CHANGE_PERIOD,
                        0.55f * playerHealth / player.getMaxHealth(), 1.0f);
                break;
            case WHITE:
            default:
                color = 0xDDDDDD;
                break;
        }
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1f);
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
        GlStateManager.color(r, g, b, a);
        drawTexturedModalRect(x, y, textureX, textureY, width, height);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private int getColorForRow(int row) {
        return Config.HEART_COLORS[row % Config.HEART_COLORS.length];
    }

    private boolean showEffectHearts(EntityPlayer player) {
        return player.isPotionActive(MobEffects.POISON) || player.isPotionActive(MobEffects.WITHER);
    }

    private int effectHeartColor(EntityPlayer player) {
        if (player.isPotionActive(MobEffects.WITHER))
            return 0x663E47;
        if (player.isPotionActive(MobEffects.POISON))
            return 0x4E9331;
        return 0xFFFFFF;
    }
}
