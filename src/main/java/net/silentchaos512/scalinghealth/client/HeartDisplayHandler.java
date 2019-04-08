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
import net.silentchaos512.lib.event.ClientTicks;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;

import java.awt.*;
import java.util.Random;

/**
 * Handles display of regular and absorption hearts.
 * <p>For future reference, much of the vanilla code can be found in {@link GuiIngameForge}.</p>
 */
public final class HeartDisplayHandler extends Gui {
    public enum TextStyle {
        DISABLED(1f) {
            @Override
            public String textFor(float current, float max) {
                return "";
            }
        },
        ROWS(0.65f) {
            @Override
            public String textFor(float current, float max) {
                return (int) Math.ceil(current / 20) + "x";
            }
        },
        HEALTH_AND_MAX(0.5f) {
            @Override
            public String textFor(float current, float max) {
                if (max == 0) return HEALTH_ONLY.textFor(current, max);
                return Math.round(current) + "/" + Math.round(max);
            }
        },
        HEALTH_ONLY(0.5f) {
            @Override
            public String textFor(float current, float max) {
                return String.valueOf(Math.round(current));
            }
        };

        private static final String COMMENT = "Determines what the text next to your hearts will display. DISABLED will display nothing, ROWS will display the number of remaining rows that have health left, and HEALTH_AND_MAX will display your actual health and max health values (for absorption, there is no max value). HEALTH_ONLY displays just the current amount.";

        private final float scale;

        TextStyle(float scale) {
            this.scale = scale;
        }

        public static TextStyle loadFromConfig(ConfigBase config, String name, TextStyle defaultValue) {
            return config.loadEnum(name, Config.CAT_CLIENT, TextStyle.class, defaultValue, COMMENT);
        }

        public abstract String textFor(float current, float max);
    }

    public enum TextColor {
        GREEN_TO_RED, WHITE, PSYCHEDELIC, SOLID;

        public static final String COMMENT = "Determines the color of the text next to your hearts. GREEN_TO_RED displays green at full health, and moves to red as you lose health (does not work with absorption). WHITE will just be good old fashioned white text. Set to PSYCHEDELIC if you want to taste the rainbow. SOLID is a fixed color";

        public static TextColor loadFromConfig(ConfigBase config, String name, TextColor defaultValue) {
            return config.loadEnum(name, Config.CAT_CLIENT, TextColor.class, defaultValue, COMMENT);
        }
    }

    public enum AbsorptionHeartStyle {
        SHIELD, GOLD_OUTLINE, VANILLA;

        public static final String COMMENT = "Determines how absorption hearts should be rendered.";

        public static AbsorptionHeartStyle loadDromConfig(ConfigBase config) {
            return config.loadEnum("Absorption Heart Style", Config.CAT_CLIENT, AbsorptionHeartStyle.class, SHIELD, COMMENT);
        }
    }

    public static final HeartDisplayHandler INSTANCE = new HeartDisplayHandler();

    private static final float COLOR_CHANGE_PERIOD = 150;
    private static final ResourceLocation TEXTURE = new ResourceLocation(ScalingHealth.MOD_ID_LOWER, "textures/gui/hud.png");

    private long lastSystemTime = 0;
    private int playerHealth = 0;
    private int lastPlayerHealth = 0;
    private Random rand = new Random();

    private HeartDisplayHandler() {}

    @SubscribeEvent(receiveCanceled = true)
    public void onHealthBar(RenderGameOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;

        // Health text
        if (event.getType() == ElementType.TEXT && mc.playerController.gameIsSurvivalOrAdventure()) {
            // Draw health string?
            if (Config.Client.Hearts.textStyle != TextStyle.DISABLED) {
                renderHealthText(event, player.getHealth(), player.getMaxHealth(),
                        -91 + Config.Client.Hearts.textOffsetX, -38 + Config.Client.Hearts.textOffsetY,
                        Config.Client.Hearts.textStyle, Config.Client.Hearts.textColor);
            }
            // Draw absorption amount string?
            if (Config.Client.Hearts.absorbTextStyle != TextStyle.DISABLED && player.getAbsorptionAmount() > 0) {
                renderHealthText(event, player.getAbsorptionAmount(), 0,
                        -91 + Config.Client.Hearts.absorbTextOffsetX, -49 + Config.Client.Hearts.absorbTextOffsetY,
                        Config.Client.Hearts.absorbTextStyle, Config.Client.Hearts.absorbTextColor);
            }
        }

        // Hearts
        if (event.getType() == ElementType.HEALTH && Config.Client.Hearts.customHeartRendering) {
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
        int updateCounter = ClientTicks.ticksInGame;

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
        mc.renderEngine.bindTexture(TEXTURE);
        health = MathHelper.ceil(player.getHealth());
        int rowCount = getCustomHeartRowCount(health);
        int maxHealthRows = getCustomHeartRowCount((int) player.getMaxHealth());

        for (int row = Math.max(0, rowCount - 2); row < rowCount; ++row) {
            int actualRow = row + (Config.Client.Hearts.replaceVanillaRow ? 0 : 1);
            int renderHearts = Math.min((health - 20 * actualRow) / 2, 10);
            int rowColor = getColorForRow(row, false);
            boolean anythingDrawn;

            // Draw the hearts
            int j;
            int y;
            for (j = 0; j < renderHearts; ++j) {
                y = top + (j == regen ? -2 : 0);
                if (health <= 4)
                    y += lowHealthBob[MathHelper.clamp(j, 0, lowHealthBob.length - 1)];
                drawTexturedModalRect(left + 8 * j, y, 0, potionOffset, 9, 9, rowColor);
            }
            anythingDrawn = j > 0;

            // Half heart on the end?
            if (health % 2 == 1 && renderHearts < 10) {
                y = top + (j == regen ? -2 : 0);
                if (health <= 4)
                    y += lowHealthBob[MathHelper.clamp(j, 0, lowHealthBob.length - 1)];
                drawTexturedModalRect(left + 8 * renderHearts, y, 9, potionOffset, 9, 9, rowColor);
                anythingDrawn = true;
            }

            // Outline for last heart, to make seeing max health a little easier.
            if (Config.Client.Hearts.lastHeartOutline && anythingDrawn && row == maxHealthRows - 1) {
                // Get position of last partial/full heart
                j = (int) (Math.ceil(player.getMaxHealth() % 20f / 2f)) - 1;
                if (j < 0) j += 10;
                y = top + (j == regen ? -2 : 0);
                if (health <= 4)
                    y += lowHealthBob[MathHelper.clamp(j, 0, lowHealthBob.length - 1)];
                drawTexturedModalRect(left + 8 * j, y, 17, 9, 9, 9, Config.Client.Hearts.lastHeartOutlineColor);
            }
        }

        for (int i = 0; i < 10 && i < Math.ceil(health / 2f); ++i) {
            int y = top + (i == regen ? -2 : 0);
            if (health <= 4)
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

        /* ==========================
         * Absorption hearts override
         * ========================== */

        if (Config.Client.Hearts.absorptionStyle != AbsorptionHeartStyle.VANILLA) {
            int absorbCeil = (int) Math.ceil(absorb);
            rowCount = (int) Math.ceil(absorb / 20);

            // Dark underlay for first row
            int texX = 17;
            int texY = Config.Client.Hearts.absorptionStyle == AbsorptionHeartStyle.SHIELD ? 45 : 54;
            for (int i = 0; i < 10 && i < absorb / 2; ++i) {
                int y = top - 10 + (i == regen - 10 ? -2 : 0);
                drawTexturedModalRect(left + 8 * i, y, texX, texY, 9, 9, 0xFFFFFF);
            }

            // Draw the top two absorption rows, just the basic "hearts"
            texX = Config.Client.Hearts.absorptionStyle == AbsorptionHeartStyle.SHIELD ? 26 : 0;
            texY = Config.Client.Hearts.absorptionStyle == AbsorptionHeartStyle.SHIELD ? 0 : potionOffset;
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
                if (Config.Client.Hearts.absorptionStyle == AbsorptionHeartStyle.SHIELD) {
                    // Golden hearts in center (shield style only)
                    drawTexturedModalRect(left + 8 * i, y, 17, 36, 9, 9, 0xFFFFFF);
                } else if (Config.Client.Hearts.absorptionStyle == AbsorptionHeartStyle.GOLD_OUTLINE) {
                    // Golden outline
                    drawTexturedModalRect(left + 8 * i, y, 17, 27, 9, 9, 0xFFFFFF);
                }
                // Shiny glint on top, same as hearts.
                if (!hardcoreMode || Config.Client.Hearts.absorptionStyle == AbsorptionHeartStyle.SHIELD) {
                    drawTexturedModalRect(left + 8 * i, y, 17, 0, 9, 9, 0xCCFFFFFF);
                }
            }
        }

        GlStateManager.disableBlend();
        mc.renderEngine.bindTexture(Gui.ICONS);
    }

    private static int getCustomHeartRowCount(int health) {
        return Config.Client.Hearts.replaceVanillaRow ? MathHelper.ceil(health / 20f) : health / 20;
    }

    private void drawVanillaHearts(int health, boolean highlight, int healthLast, float healthMax, int rowHeight, int left, int top, int regen, int[] lowHealthBob, int TOP, int BACKGROUND, int MARGIN) {
        float absorb = MathHelper.ceil(Minecraft.getMinecraft().player.getAbsorptionAmount());
        float absorbRemaining = absorb;
        float healthTotal = health + absorb;

        int iStart = MathHelper.ceil((healthMax + (Config.Client.Hearts.absorptionStyle == AbsorptionHeartStyle.VANILLA ? absorb : 0)) / 2f) - 1;
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

            // TODO: Does this fix the rendering issues introduced in 1.3.27?
            if (absorbRemaining > 0f && Config.Client.Hearts.absorptionStyle == AbsorptionHeartStyle.VANILLA) {
                if (absorbRemaining == absorb && absorb % 2f == 1f) {
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

    private void renderHealthText(RenderGameOverlayEvent event, float current, float max, int offsetX, int offsetY, TextStyle style, TextColor styleColor) {
        final float scale = style.scale;
        final int width = event.getResolution().getScaledWidth();
        final int height = event.getResolution().getScaledHeight();
        final int left = (int) ((width / 2 + offsetX) / scale);
        // GuiIngameForge.left_height == 59 in normal cases. Making it a constant should fix some issues.
        final int top = (int) ((height + offsetY + (1 / scale)) / scale);

        // Draw health string
        String healthString = style.textFor(current, max);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int stringWidth = fontRenderer.getStringWidth(healthString);
        int color;
        float divisor = max == 0 ? current : max;
        switch (styleColor) {
            case GREEN_TO_RED:
                color = Color.HSBtoRGB(0.34f * current / divisor, 0.7f, 1.0f);
                break;
            case PSYCHEDELIC:
                color = Color.HSBtoRGB(
                        (ClientTicks.ticksInGame % COLOR_CHANGE_PERIOD) / COLOR_CHANGE_PERIOD,
                        0.55f * current / divisor, 1.0f);
                break;
            case SOLID:
                color = Config.Client.Hearts.textSolidColor;
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

    private int getColorForRow(int row, boolean absorption) {
        int[] colors = absorption ? Config.Client.Hearts.absorptionHeartColors : Config.Client.Hearts.heartColors;
        int index = Config.Client.Hearts.heartColorLooping
                ? row % colors.length
                : MathHelper.clamp(row, 0, colors.length - 1);
        return colors[index];
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
