package net.silentchaos512.scalinghealth.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.common.Mod;
import net.silentchaos512.scalinghealth.ScalingHealth;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ScalingHealth.MOD_ID, value = Dist.CLIENT)
public class KeyManager {
    public static final KeyMapping TOGGLE_DIFF = new KeyMapping("key.scalinghealth.difficultyMeter", GLFW.GLFW_KEY_Z, ScalingHealth.MOD_NAME);

    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_DIFF);
    }
}
