package net.silentchaos512.scalinghealth;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.silentchaos512.lib.SilentLib;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.lib.util.LocalizationHelper;
import net.silentchaos512.lib.util.LogHelper;
import net.silentchaos512.scalinghealth.command.CommandScalingHealth;
import net.silentchaos512.scalinghealth.init.ModItems;
import net.silentchaos512.scalinghealth.proxy.ScalingHealthCommonProxy;

import java.util.Random;

@Mod(modid = ScalingHealth.MOD_ID_LOWER,
        name = ScalingHealth.MOD_NAME,
        version = ScalingHealth.VERSION,
        dependencies = ScalingHealth.DEPENDENCIES,
        acceptedMinecraftVersions = ScalingHealth.ACCEPTED_MC_VERSIONS,
        guiFactory = "net.silentchaos512.scalinghealth.gui.GuiFactoryScalingHealth")
public class ScalingHealth {

    public static final String MOD_ID_OLD = "ScalingHealth";
    public static final String MOD_ID_LOWER = "scalinghealth";
    public static final String MOD_NAME = "Scaling Health";
    public static final String VERSION = "1.3.20";
    public static final String VERSION_SILENTLIB = "2.3.6";
    public static final int BUILD_NUM = 0;
    public static final String DEPENDENCIES = "required-after:silentlib@[" + VERSION_SILENTLIB + ",);after:morpheus";
    public static final String ACCEPTED_MC_VERSIONS = "[1.12,1.12.2]";
    public static final String RESOURCE_PREFIX = MOD_ID_LOWER + ":";

    public static final String GAME_RULE_DIFFICULTY = "ScalingHealthDifficulty";

    public static Random random = new Random();
    public static LogHelper logHelper = new LogHelper(MOD_NAME, BUILD_NUM);
    public static LocalizationHelper localizationHelper;

    public static SRegistry registry = new SRegistry(MOD_ID_LOWER, logHelper);

    public static CreativeTabs creativeTab = registry.makeCreativeTab(MOD_ID_LOWER, () -> new ItemStack(ModItems.heart));

    @Instance(MOD_ID_LOWER)
    public static ScalingHealth instance;

    @SidedProxy(clientSide = "net.silentchaos512.scalinghealth.proxy.ScalingHealthClientProxy", serverSide = "net.silentchaos512.scalinghealth.proxy.ScalingHealthCommonProxy")
    public static ScalingHealthCommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        registry.setMod(this);
        localizationHelper = new LocalizationHelper(MOD_ID_LOWER).setReplaceAmpersand(true);
        SilentLib.instance.registerLocalizationHelperForMod(MOD_ID_LOWER, localizationHelper);

        registry.recipes.setJsonHellMode(0 == getBuildNumber());

        proxy.preInit(registry, event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(registry, event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(registry, event);
    }

    @EventHandler
    public void onServerLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandScalingHealth());
    }

    @EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null) {
            server.worlds[0].getGameRules().setOrCreateGameRule(GAME_RULE_DIFFICULTY, "true");
        }
    }

    public int getBuildNumber() {
        return BUILD_NUM;
    }
}
