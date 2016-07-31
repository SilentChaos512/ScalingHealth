package net.silentchaos512.scalinghealth;

import java.util.Random;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.silentchaos512.lib.SilentLib;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.lib.util.LocalizationHelper;
import net.silentchaos512.lib.util.LogHelper;
import net.silentchaos512.scalinghealth.command.CommandScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.init.ModItems;
import net.silentchaos512.scalinghealth.network.PacketScalingHealth;
import net.silentchaos512.scalinghealth.proxy.ScalingHealthCommonProxy;

//@formatter:off
@Mod(modid = ScalingHealth.MOD_ID,
  name = ScalingHealth.MOD_NAME,
  version = ScalingHealth.VERSION,
  dependencies = ScalingHealth.DEPENDENCIES,
  guiFactory = "net.silentchaos512.scalinghealth.gui.GuiFactoryScalingHealth")
//@formatter:on
public class ScalingHealth {

  public static final String MOD_ID = "ScalingHealth";
  public static final String MOD_ID_LOWER = "scalinghealth";
  public static final String MOD_NAME = "Scaling Health";
  public static final String VERSION = "@VERSION@";
  public static final String DEPENDENCIES = "required-after:Forge@[12.17.0.1976,);required-after:SilentLib;";
  public static final String RESOURCE_PREFIX = MOD_ID.toLowerCase() + ":";

  public static SimpleNetworkWrapper networkManager;

  public static Random random = new Random();
  public static LogHelper logHelper = new LogHelper(MOD_NAME);
  public static LocalizationHelper localizationHelper;

  public static SRegistry registry = new SRegistry(MOD_ID);

  public static CreativeTabs creativeTab = new CreativeTabs("tab" + MOD_ID) {

    @Override
    public Item getTabIconItem() {

      return Items.ACACIA_BOAT;
    }
  };

  @Instance(MOD_ID_LOWER)
  public static ScalingHealth instance;

  @SidedProxy(clientSide = "net.silentchaos512.scalinghealth.proxy.ScalingHealthClientProxy", serverSide = "net.silentchaos512.scalinghealth.proxy.ScalingHealthCommonProxy")
  public static ScalingHealthCommonProxy proxy;

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {

    localizationHelper = new LocalizationHelper(MOD_ID).setReplaceAmpersand(true);
    SilentLib.instance.registerLocalizationHelperForMod(MOD_ID, localizationHelper);

    networkManager = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
    networkManager.registerMessage(PacketScalingHealth.Handler.class, PacketScalingHealth.class, 0,
        Side.SERVER);
    networkManager.registerMessage(PacketScalingHealth.Handler.class, PacketScalingHealth.class, 0,
        Side.CLIENT);

    ConfigScalingHealth.init(event.getSuggestedConfigurationFile());

    ModItems.init(registry);

    // TODO: Achievements?

    // TODO: World generator?

    proxy.preInit(registry);
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {

    // TODO: Save config?

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
}
