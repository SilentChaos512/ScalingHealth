package net.silentchaos512.scalinghealth;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

@Mod(ScalingHealth.MOD_ID)
public class ScalingHealth {
    public static final String MOD_ID_OLD = "ScalingHealth";
    public static final String MOD_ID = "scalinghealth";
    public static final String MOD_NAME = "Scaling Health";
    public static final String VERSION = "2.0.1";
    public static final int BUILD_NUM = 0;
    public static final String RESOURCE_PREFIX = MOD_ID + ":";

    public static final Random random = new Random();

    private static ScalingHealth INSTANCE;
    private static SideProxy PROXY;

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public ScalingHealth() {
        INSTANCE = this;
        PROXY = DistExecutor.runForDist(() -> () -> new SideProxy.Client(), () -> () -> new SideProxy.Server());
    }

    public static ResourceLocation res(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
