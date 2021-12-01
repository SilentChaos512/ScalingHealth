package net.silentchaos512.scalinghealth.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import net.silentchaos512.scalinghealth.ScalingHealth;
import org.lwjgl.glfw.GLFW;

public class KeyManager {
    public static final KeyMapping TOGGLE_DIFF = new KeyMapping("key.scalinghealth.difficultyMeter", GLFW.GLFW_KEY_Z, ScalingHealth.MOD_NAME);

    public static void registerBindings() {
        ClientRegistry.registerKeyBinding(TOGGLE_DIFF);
    }
}
