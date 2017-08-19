package net.silentchaos512.scalinghealth.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public class DifficultyDisplayHandler extends Gui {

  public static final DifficultyDisplayHandler INSTANCE = new DifficultyDisplayHandler();

  public static final ResourceLocation TEXTURE = new ResourceLocation(ScalingHealth.MOD_ID_LOWER, "textures/gui/hud.png");

  int lastDifficultyDisplayed = -100;
  int lastAreaDifficultyDisplayed = -100;
  int lastUpdateTime = Integer.MIN_VALUE;
  int currentTime = 0;

  public void showBar() {

    this.lastUpdateTime = Integer.MIN_VALUE;
    this.lastDifficultyDisplayed = -100;
  }

  @SubscribeEvent
  public void onRenderOverlay(RenderGameOverlayEvent.Post event) {

    if (event.getType() != ElementType.TEXT || ConfigScalingHealth.DIFFICULTY_MAX <= 0 || !ConfigScalingHealth.RENDER_DIFFICULTY_METER)
      return;

    Minecraft mc = Minecraft.getMinecraft();
    EntityPlayer player = mc.player;

    int width = event.getResolution().getScaledWidth();
    int height = event.getResolution().getScaledHeight();

    PlayerData data = player != null ? SHPlayerDataHandler.get(player) : null;
    if (data == null)
      return;

    int difficulty = (int) data.getDifficulty();
    int areaDifficulty = MathHelper.clamp((int) ConfigScalingHealth.AREA_DIFFICULTY_MODE.getAreaDifficulty(player.world, player.getPosition()), 0,
        (int) ConfigScalingHealth.DIFFICULTY_MAX);
    int timeSinceLastUpdate = ClientTickHandler.ticksInGame - lastUpdateTime;

    if (difficulty != lastDifficultyDisplayed) {
      lastDifficultyDisplayed = difficulty;
      lastUpdateTime = ClientTickHandler.ticksInGame;
    }
    if (areaDifficulty < lastAreaDifficultyDisplayed - 10 || areaDifficulty > lastAreaDifficultyDisplayed + 10 && timeSinceLastUpdate > 1200) {
      lastAreaDifficultyDisplayed = areaDifficulty;
      lastUpdateTime = ClientTickHandler.ticksInGame;
    }

    currentTime = ClientTickHandler.ticksInGame;
    if (ConfigScalingHealth.RENDER_DIFFICULTY_METER_ALWAYS || currentTime - lastUpdateTime < ConfigScalingHealth.DIFFICULTY_METER_DISPLAY_TIME) {
      GlStateManager.enableBlend();

      mc.renderEngine.bindTexture(TEXTURE);

      GlStateManager.pushMatrix();
      // GlStateManager.scale(1f, 0.5f, 1f);

      int posX = ConfigScalingHealth.DIFFICULTY_METER_POS_X; // 5;
      if (posX < 0)
        posX = posX + width - 64;
      int posY = ConfigScalingHealth.DIFFICULTY_METER_POS_Y; // height - 30;
      if (posY < 0)
        posY = posY + height - 12;

      // Frame
      drawTexturedModalRect(posX, posY, 192, 0, 64, 12, 0xFFFFFF);

      // Area Difficulty
      int barLength = (int) (60 * areaDifficulty / ConfigScalingHealth.DIFFICULTY_MAX);
      drawTexturedModalRect(posX + 2, posY + 2, 194, 14, barLength, 6, 0xFFFFFF);

      // Difficulty
      barLength = (int) (60 * difficulty / ConfigScalingHealth.DIFFICULTY_MAX);
      drawTexturedModalRect(posX + 2, posY + 8, 194, 20, barLength, 2, 0xFFFFFF);

      // Text
      float textScale = 0.6f;
      GlStateManager.pushMatrix();
      GlStateManager.scale(textScale, textScale, 1.0f);
      mc.fontRenderer.drawStringWithShadow("DIFFICULTY", posX / textScale + 4, posY / textScale - 9, 0xFFFFFF);
      // Text Difficulty
      String str = String.format("%d", areaDifficulty);
      int strWidth = mc.fontRenderer.getStringWidth(str);
      mc.fontRenderer.drawStringWithShadow(str, posX / textScale + 104 - strWidth, posY / textScale - 9, 0xAAAAAA);
      GlStateManager.popMatrix();

      GlStateManager.popMatrix();
      GlStateManager.disableBlend();
    }
  }

  protected void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height, int color) {

    float r = ((color >> 16) & 255) / 255f;
    float g = ((color >> 8) & 255) / 255f;
    float b = (color & 255) / 255f;
    GlStateManager.color(r, g, b);
    drawTexturedModalRect(x, y, textureX, textureY, width, height);
    GlStateManager.color(1f, 1f, 1f);
  }
}
