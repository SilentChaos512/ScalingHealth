package net.silentchaos512.scalinghealth.gui;

import com.typesafe.config.Config;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;

public class GuiConfigScalingHealth extends GuiConfig {

  public GuiConfigScalingHealth(GuiScreen parent) {

    super(parent, ConfigScalingHealth.getConfigElements(), ScalingHealth.MOD_ID_LOWER, false, false,
        "Scaling Health Config");
  }

  @Override
  public void initGui() {

    // You can add buttons and initialize fields here
    super.initGui();
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {

    // You can do things like create animations, draw additional elements, etc. here
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  protected void actionPerformed(GuiButton button) {

    // You can process any additional buttons you may have added here
    super.actionPerformed(button);
  }
}
