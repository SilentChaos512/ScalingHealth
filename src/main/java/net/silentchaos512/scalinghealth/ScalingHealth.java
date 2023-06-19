package net.silentchaos512.scalinghealth;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.silentchaos512.scalinghealth.capability.IDifficultyAffected;
import net.silentchaos512.scalinghealth.capability.IDifficultySource;
import net.silentchaos512.scalinghealth.capability.IPetData;
import net.silentchaos512.scalinghealth.capability.IPlayerData;
import net.silentchaos512.scalinghealth.command.ModCommands;
import net.silentchaos512.scalinghealth.config.SHConfig;
import net.silentchaos512.scalinghealth.loot.conditions.EntityGroupCondition;
import net.silentchaos512.scalinghealth.loot.conditions.SHMobProperties;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.objects.Registration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

@Mod(ScalingHealth.MOD_ID)
public class ScalingHealth {
    public static final String MOD_ID = "scalinghealth";
    public static final String MOD_NAME = "Scaling Health";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static final Random RANDOM = new Random();

    public ScalingHealth() {
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();

        Registration.register(modbus);

        SHConfig.register();
        Network.init();

        modbus.addListener(this::registerCaps);
        modbus.addListener(this::reloadConfig);

        MinecraftForge.EVENT_BUS.addListener(this::registerCommandsEvent);
    }

    private void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(IDifficultyAffected.class);
        event.register(IDifficultySource.class);
        event.register(IPlayerData.class);
        event.register(IPetData.class);
    }

    private void registerCommandsEvent(RegisterCommandsEvent event) {
        ModCommands.registerAll(event);
    }

    private void reloadConfig(ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() == ModConfig.Type.CLIENT)
            SHConfig.CLIENT.reload();
    }

    public static ResourceLocation getId(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
