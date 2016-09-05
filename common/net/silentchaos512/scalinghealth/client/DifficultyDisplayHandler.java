package net.silentchaos512.scalinghealth.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public class DifficultyDisplayHandler extends Gui {

  public static final ResourceLocation TEXTURE = new ResourceLocation(ScalingHealth.MOD_ID_LOWER,
      "textures/gui/hud.png");

  int lastDifficultyDisplayed = -100;
  int lastAreaDifficultyDisplayed = -100;
  int lastUpdateTime = 0;
  int currentTime = 0;

  @SubscribeEvent
  public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {

    // TODO: Configs?
    if (event.getType() != ElementType.TEXT || ConfigScalingHealth.DIFFICULTY_MAX <= 0)
      return;

    Minecraft mc = Minecraft.getMinecraft();
    EntityPlayer player = mc.thePlayer;

    int width = event.getResolution().getScaledWidth();
    int height = event.getResolution().getScaledHeight();

    PlayerData data = player != null ? SHPlayerDataHandler.get(player) : null;
    if (data == null)
      return;

    int difficulty = (int) data.getDifficulty();
    int areaDifficulty = (int) ConfigScalingHealth.AREA_DIFFICULTY_MODE
        .getAreaDifficulty(player.worldObj, player.getPosition());

    if (difficulty != lastDifficultyDisplayed) {
      lastDifficultyDisplayed = difficulty;
      lastUpdateTime = ClientTickHandler.ticksInGame;
    }
    if (areaDifficulty < lastAreaDifficultyDisplayed - 10
        || areaDifficulty > lastAreaDifficultyDisplayed + 10) {
      lastAreaDifficultyDisplayed = areaDifficulty;
      lastUpdateTime = ClientTickHandler.ticksInGame;
    }

    currentTime = ClientTickHandler.ticksInGame;
    if (currentTime - lastUpdateTime < 160) { // TODO: Config?
      GlStateManager.enableBlend();

      mc.renderEngine.bindTexture(TEXTURE);

      GlStateManager.pushMatrix();
      // GlStateManager.scale(1f, 0.5f, 1f);

      int posX = width / 2 - 32;
      int posY = height - GuiIngameForge.left_height - 14;
      // Frame
      drawTexturedModalRect(posX, posY, 192, 0, 64, 12, 0xFFFFFF);
      // Area Difficulty
      int barLength = (int) (60 * areaDifficulty / ConfigScalingHealth.DIFFICULTY_MAX);
      drawTexturedModalRect(posX + 2, posY + 2, 194, 14, barLength, 6, 0xFFFFFF);
      // Difficulty
      barLength = (int) (60 * difficulty / ConfigScalingHealth.DIFFICULTY_MAX);
      drawTexturedModalRect(posX + 2, posY + 8, 194, 20, barLength, 2, 0xFFFFFF);

      GlStateManager.popMatrix();
      GlStateManager.disableBlend();
    }
  }

  protected void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width,
      int height, int color) {

    float r = ((color >> 16) & 255) / 255f;
    float g = ((color >> 8) & 255) / 255f;
    float b = (color & 255) / 255f;
    GlStateManager.color(r, g, b);
    drawTexturedModalRect(x, y, textureX, textureY, width, height);
    GlStateManager.color(1f, 1f, 1f);
  }
}
