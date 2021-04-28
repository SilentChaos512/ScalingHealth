package net.silentchaos512.scalinghealth;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootConditionType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.silentchaos512.scalinghealth.capability.DifficultyAffectedCapability;
import net.silentchaos512.scalinghealth.capability.DifficultySourceCapability;
import net.silentchaos512.scalinghealth.capability.PetHealthCapability;
import net.silentchaos512.scalinghealth.capability.PlayerDataCapability;
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

    public static final ItemGroup SH = new ItemGroup(MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Registration.HEART_CRYSTAL.get());
        }
    };

    public ScalingHealth() {
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();

        Registration.register(modbus);

        SHConfig.register();
        Network.init();

        modbus.addListener(this::commonSetup);
        modbus.addGenericListener(GlobalLootModifierSerializer.class, this::registerLootModSerializers);

        MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);
    }

    private void registerLootModSerializers(RegistryEvent.Register<GlobalLootModifierSerializer<?>> event) {
        Registry.register(Registry.LOOT_CONDITION_TYPE, SHMobProperties.NAME, new LootConditionType(new SHMobProperties.Serializer()));
        Registry.register(Registry.LOOT_CONDITION_TYPE, EntityGroupCondition.NAME, new LootConditionType(new EntityGroupCondition.Serializer()));
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        DifficultyAffectedCapability.register();
        DifficultySourceCapability.register();
        PlayerDataCapability.register();
        PetHealthCapability.register();
    }

    private void serverAboutToStart(FMLServerAboutToStartEvent event) {
        ModCommands.registerAll(event.getServer().getCommandManager().getDispatcher());
    }

    public static ResourceLocation getId(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
