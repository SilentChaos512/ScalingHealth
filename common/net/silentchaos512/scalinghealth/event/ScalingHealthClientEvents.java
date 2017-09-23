package net.silentchaos512.scalinghealth.event;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.entity.EntityBlightFire;
import net.silentchaos512.scalinghealth.lib.EnumAreaDifficultyMode;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public class ScalingHealthClientEvents {

  @SubscribeEvent
  public void renderTick(RenderGameOverlayEvent.Post event) {

    if (Minecraft.getMinecraft().world == null || event.getType() != ElementType.ALL)
      return;

    if (!ConfigScalingHealth.DEBUG_MODE)
      return;

    ScaledResolution res = event.getResolution();
    int width = res.getScaledWidth();
    int height = res.getScaledHeight();

    FontRenderer fontRender = Minecraft.getMinecraft().fontRenderer;

    GL11.glPushMatrix();
    float scale = 0.6f;
    GlStateManager.scale(scale, scale, 1.0f);

    String text = getDebugText();
    int y = 3;
    for (String line : text.split("\n")) {
      String[] array = line.split("=");
      if (array.length == 2) {
        fontRender.drawString(array[0].trim(), 3, y, 0xFFFFFF);
        fontRender.drawString(array[1].trim(), 90, y, 0xFFFFFF);
      } else {
        fontRender.drawString(line, 3, y, 0xFFFFFF);
      }
      y += 10;
    }

    GL11.glPopMatrix();
  }

  private String getDebugText() {

    World world = Minecraft.getMinecraft().world;
    EntityPlayer player = Minecraft.getMinecraft().player;
    PlayerData data = SHPlayerDataHandler.get(player);
    EnumAreaDifficultyMode areaMode = ConfigScalingHealth.AREA_DIFFICULTY_MODE;
    if (data == null)
      return "Player data is null!";

    String ret = "";

    ret += String.format("Area Difficulty = %.4f (%s)\n",
        areaMode.getAreaDifficulty(world, player.getPosition()), areaMode.name());
    ret += String.format("Player Difficulty = %.4f\n", data.getDifficulty());
    ret += "Player Health = " + player.getHealth() + " / " + player.getMaxHealth() + "\n";

    // Display all health attribute modifiers.
    IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
    if (attr.getModifiers().size() > 0) {
      for (AttributeModifier mod : attr.getModifiers()) {
        ret += "         " + mod.toString() + "\n";
      }
    } else {
      ret += "        No modifiers! That should not happen.";
    }

    int regenTimer = PlayerBonusRegenHandler.INSTANCE.getTimerForPlayer(player);
    ret += String.format("Regen Timer = %d (%ds)", regenTimer, regenTimer / 20) + "\n";
    ret += String.format("Food = %d (%.2f)", player.getFoodStats().getFoodLevel(),
        player.getFoodStats().getSaturationLevel()) + "\n";

    // Blight count
    int blightCount = world.getEntities(EntityLivingBase.class, e -> BlightHandler.isBlight(e))
        .size();
    int blightFires = world.getEntities(EntityBlightFire.class, e -> true).size();
    ret += String.format("Blights (Fires) = %d (%d)", blightCount, blightFires);

    return ret;
  }
}
