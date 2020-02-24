package net.silentchaos512.scalinghealth.client.KeyBinds;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.silentchaos512.scalinghealth.ScalingHealth;
import org.lwjgl.glfw.GLFW;

public class KeyManager {
    public static final KeyBinding toggleDiff = new KeyBinding("key.scalinghealth.difficulty", GLFW.GLFW_KEY_Z, ScalingHealth.MOD_NAME);

    public static void registerBindings(){
        ClientRegistry.registerKeyBinding(toggleDiff);
    }
}
