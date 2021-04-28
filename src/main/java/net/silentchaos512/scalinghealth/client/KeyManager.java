package net.silentchaos512.scalinghealth.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.silentchaos512.scalinghealth.ScalingHealth;
import org.lwjgl.glfw.GLFW;

public class KeyManager {
    public static final KeyBinding TOGGLE_DIFF = new KeyBinding("key.scalinghealth.difficultyMeter", GLFW.GLFW_KEY_Z, ScalingHealth.MOD_NAME);

    public static void registerBindings() {
        ClientRegistry.registerKeyBinding(TOGGLE_DIFF);
    }
}
