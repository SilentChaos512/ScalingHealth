package net.silentchaos512.scalinghealth.gui;

import net.minecraft.client.gui.GuiScreen;
import net.silentchaos512.lib.gui.config.GuiFactorySL;

public class GuiFactoryScalingHealth extends GuiFactorySL {
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return GuiConfigScalingHealth.class;
    }
}
