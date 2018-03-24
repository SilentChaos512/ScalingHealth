package net.silentchaos512.scalinghealth.client;

import java.awt.Color;
import java.util.Random;

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
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;

public class HeartDisplayHandler extends Gui {

  private static final float COLOR_CHANGE_PERIOD = 150;

  public static enum TextStyle {

    DISABLED, ROWS, HEALTH_AND_MAX;

    public static TextStyle loadFromConfig(Configuration config) {

      String[] validValues = new String[values().length];
      for (TextStyle style : values()) {
        validValues[style.ordinal()] = style.name();
      }

      String value = config.getString("Health Text Style", ConfigScalingHealth.CAT_CLIENT,
          ROWS.name(),
          "Determines what the text next to your hearts will display. DISABLED will display"
              + " nothing, ROWS will display the number of remaining rows that have health left,"
              + " and HEALTH_AND_MAX will display your actual health and max health values.",
          validValues);

      for (TextStyle style : values()) {
        if (value.equalsIgnoreCase(style.name())) {
          return style;
        }
      }

      return ROWS;
    }
  }

  public static enum TextColor {

    GREEN_TO_RED, WHITE, PSYCHEDELIC;

    public static TextColor loadFromConfig(Configuration config) {

      String[] validValues = new String[values().length];
      for (TextColor style : values()) {
        validValues[style.ordinal()] = style.name();
      }

      String value = config.getString("Health Text Color", ConfigScalingHealth.CAT_CLIENT,
          GREEN_TO_RED.name(),
          "Determines the color of the text next to your hearts. GREEN_TO_RED displays green at"
              + " full health, and moves to red as you lose health. WHITE will just be good old"
              + " fashioned white text. Set to PSYCHEDELIC if you want to taste the rainbow.",
          validValues);

      for (TextColor style : values()) {
        if (value.equalsIgnoreCase(style.name())) {
          return style;
        }
      }

      return GREEN_TO_RED;
    }
  }

  public static final ResourceLocation TEXTURE = new ResourceLocation(ScalingHealth.MOD_ID_LOWER,
      "textures/gui/hud.png");

  public static HeartDisplayHandler INSTANCE = null;

  long lastSystemTime = 0;
  long healthUpdateCounter = 0;
  int updateCounter = 0;
  int playerHealth = 0;
  int lastPlayerHealth = 0;
  Random rand = new Random();

  public HeartDisplayHandler() {

    if (INSTANCE == null) {
      INSTANCE = this;
    }
  }

  @SubscribeEvent(receiveCanceled = true)
  public void onHealthBar(RenderGameOverlayEvent.Pre event) {

    Minecraft mc = Minecraft.getMinecraft();
    EntityPlayer player = mc.player;

    TextStyle style = ConfigScalingHealth.HEART_DISPLAY_TEXT_STYLE;
    TextColor styleColor = ConfigScalingHealth.HEART_DISPLAY_TEXT_COLOR;
    if (event.getType() == ElementType.TEXT && style != TextStyle.DISABLED
        && !player.capabilities.isCreativeMode) {
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
      int color = 0xDDDDDD;
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
          color = 0xDDDDDD;
          break;
      }
      GlStateManager.pushMatrix();
      GlStateManager.scale(scale, scale, 1f);
      fontRenderer.drawStringWithShadow(healthString, left - stringWidth - 2, top, color);
      GlStateManager.popMatrix();
    }

    if (event.getType() != ElementType.HEALTH || !ConfigScalingHealth.CHANGE_HEART_RENDERING) {
      return;
    }

    event.setCanceled(true);

    final boolean hardcoreMode = mc.world.getWorldInfo().isHardcoreModeEnabled();

    int width = event.getResolution().getScaledWidth();
    int height = event.getResolution().getScaledHeight();
    GlStateManager.enableBlend();

    int health = MathHelper.ceil(player.getHealth());
    boolean highlight = player.hurtResistantTime / 3 % 2 == 1;
    updateCounter = ClientTickHandler.ticksInGame;

    if (health < playerHealth && player.hurtResistantTime > 0) {
      lastSystemTime = Minecraft.getSystemTime();
      healthUpdateCounter = updateCounter + 20;
    } else if (health > playerHealth && player.hurtResistantTime > 0) {
      lastSystemTime = Minecraft.getSystemTime();
      healthUpdateCounter = updateCounter + 10;
    }

    if (Minecraft.getSystemTime() - lastSystemTime > 1000) {
      playerHealth = health;
      lastPlayerHealth = health;
      lastSystemTime = Minecraft.getSystemTime();
    }

    playerHealth = health;
    int healthLast = lastPlayerHealth;

    IAttributeInstance attrMaxHealth = player
        .getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
    float healthMax = Math.min((float) attrMaxHealth.getAttributeValue(), 20);
    float absorb = MathHelper.ceil(player.getAbsorptionAmount());

    int healthRows = MathHelper.ceil((healthMax + absorb) / 2f / 10f);
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
    if (player.isPotionActive(MobEffects.POISON))
      MARGIN += 36;
    else if (player.isPotionActive(MobEffects.WITHER))
      MARGIN += 72;
    float absorbRemaining = absorb;

    // Draw vanilla hearts
    for (int i = MathHelper.ceil((healthMax + absorb) / 2f) - 1; i >= 0; --i) {
      int row = MathHelper.ceil((i + 1) / 10f) - 1;
      int x = left + i % 10 * 8;
      int y = top - row * rowHeight;

      if (health <= 4)
        y += rand.nextInt(2);
      if (i == regen)
        y -= 2;

      // ScalingHealth.logHelper.debug(row, String.format("%X", rowColor));

      drawTexturedModalRect(x, y, BACKGROUND, TOP, 9, 9);

      if (highlight) {
        if (i * 2 + 1 < healthLast)
          drawTexturedModalRect(x, y, MARGIN + 54, TOP, 9, 9);
        else if (i * 2 + 1 == healthLast)
          drawTexturedModalRect(x, y, MARGIN + 63, TOP, 9, 9);
      }

      if (absorbRemaining > 0f) {
        if (absorbRemaining == absorb && absorb % 2f == 1f) {
          drawTexturedModalRect(x, y, MARGIN + 153, TOP, 9, 9);
          absorbRemaining -= 1f;
        } else {
          if (i * 2 + 1 < health)
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

    int potionOffset = player.isPotionActive(MobEffects.WITHER) ? 18
        : (player.isPotionActive(MobEffects.POISON) ? 9 : 0) + (hardcoreMode ? 27 : 0);

    // Draw extra hearts (only top 2 rows)
    mc.renderEngine.bindTexture(TEXTURE);

    health = MathHelper.ceil(player.getHealth());
    int rowCount = health / 20;
    for (int i = Math.max(0, rowCount - 2); i < rowCount; ++i) {
      int renderHearts = Math.min((health - 20 * (i + 1)) / 2, 10);
      int rowColor = getColorForRow(i);
      boolean anythingDrawn = false;

      // Draw the hearts
      int j;
      int y = 0;
      for (j = 0; j < renderHearts; ++j) {
        y = 0 + (j == regen ? -2 : 0);
        drawTexturedModalRect(left + 8 * j, top + y, 0, potionOffset, 9, 9, rowColor);
      }
      anythingDrawn = j > 0;

      // Half heart on the end?
      if (health % 2 == 1 && renderHearts < 10) {
        drawTexturedModalRect(left + 8 * renderHearts, top, 9, potionOffset, 9, 9, rowColor);
        anythingDrawn = true;
      }

      // Outline for last heart, to make seeing max health a little easier.
      if (ConfigScalingHealth.LAST_HEART_OUTLINE_ENABLED && anythingDrawn
          && i == MathHelper.ceil(player.getMaxHealth() / 20) - 2) {
        j = (int) ((player.getMaxHealth() % 20) / 2) - 1;
        if (j < 0) {
          j += 10;
        }
        drawTexturedModalRect(left + 8 * j, top + y, 17, 9, 10, 9,
            ConfigScalingHealth.LAST_HEART_OUTLINE_COLOR);
      }
    }

    // Shiny glint on top of the hearts, a single white pixel in the upper left <3
    for (int i = 0; i < 10 && i < health / 2; ++i) {
      int y = 0 + (i == regen ? -2 : 0);
      drawTexturedModalRect(left + 8 * i, top + y, 17, potionOffset, 9, 9, 0xAAFFFFFF);
    }

    GlStateManager.disableBlend();
    mc.renderEngine.bindTexture(Gui.ICONS);
  }

  protected void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width,
      int height, int color) {

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

  protected int getColorForRow(int row) {

    return ConfigScalingHealth.HEART_COLORS[row % ConfigScalingHealth.HEART_COLORS.length];
  }
}
