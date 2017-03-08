package net.silentchaos512.scalinghealth;

import java.util.Random;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.silentchaos512.lib.SilentLib;
import net.silentchaos512.lib.registry.MC10IdRemapper;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.lib.util.LocalizationHelper;
import net.silentchaos512.lib.util.LogHelper;
import net.silentchaos512.scalinghealth.command.CommandScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.init.ModBlocks;
import net.silentchaos512.scalinghealth.init.ModItems;
import net.silentchaos512.scalinghealth.proxy.ScalingHealthCommonProxy;
import net.silentchaos512.scalinghealth.world.SHWorldGenerator;

//@formatter:off
@Mod(modid = ScalingHealth.MOD_ID_LOWER,
  name = ScalingHealth.MOD_NAME,
  version = ScalingHealth.VERSION,
  dependencies = ScalingHealth.DEPENDENCIES,
  acceptedMinecraftVersions = ScalingHealth.ACCEPTED_MC_VERSIONS,
  guiFactory = "net.silentchaos512.scalinghealth.gui.GuiFactoryScalingHealth")
//@formatter:on
public class ScalingHealth {

  public static final boolean DEV_ENV = true;
  public static final String MOD_ID_OLD = "ScalingHealth";
  public static final String MOD_ID_LOWER = "scalinghealth";
  public static final String MOD_NAME = "Scaling Health";
  public static final String VERSION = "@VERSION@";
  public static final String DEPENDENCIES = "" //"required-after:forge@[12.18.2.2125,);" // 13.19.1.2188
      + "required-after:silentlib" + (DEV_ENV ? ";" : "@[2.0.1,);");
  public static final String ACCEPTED_MC_VERSIONS = "[1.10.2,1.11.2]";
  public static final String RESOURCE_PREFIX = MOD_ID_LOWER + ":";

  public static SimpleNetworkWrapper networkManager;

  public static Random random = new Random();
  public static LogHelper logHelper = new LogHelper(MOD_NAME);
  public static LocalizationHelper localizationHelper;

  public static SRegistry registry = new SRegistry(MOD_ID_LOWER, logHelper);

//  public static CreativeTabs creativeTab = new CreativeTabs("tab" + MOD_ID) {
//
//    @Override
//    public Item getTabIconItem() {
//
//      return Items.ACACIA_BOAT;
//    }
//  };

  @Instance(MOD_ID_LOWER)
  public static ScalingHealth instance;

  @SidedProxy(clientSide = "net.silentchaos512.scalinghealth.proxy.ScalingHealthClientProxy", serverSide = "net.silentchaos512.scalinghealth.proxy.ScalingHealthCommonProxy")
  public static ScalingHealthCommonProxy proxy;

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {

    localizationHelper = new LocalizationHelper(MOD_ID_LOWER).setReplaceAmpersand(true);
    SilentLib.instance.registerLocalizationHelperForMod(MOD_ID_LOWER, localizationHelper);

    ConfigScalingHealth.init(event.getSuggestedConfigurationFile());
    ConfigScalingHealth.save();

    ModBlocks.init(registry);
    ModItems.init(registry);

    // TODO: Achievements?

    GameRegistry.registerWorldGenerator(new SHWorldGenerator(), 0);

    proxy.preInit(registry);
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {

    proxy.init(registry);
  }

  @EventHandler
  public void postInit(FMLPostInitializationEvent event) {

    proxy.postInit(registry);
  }

  @EventHandler
  public void onServerLoad(FMLServerStartingEvent event) {

    event.registerServerCommand(new CommandScalingHealth());
  }

  @EventHandler
  public void onMissingMapping(FMLMissingMappingsEvent event) {

    for (FMLMissingMappingsEvent.MissingMapping mismap : event.get()) {
      MC10IdRemapper.remap(mismap);
    }
  }
}
