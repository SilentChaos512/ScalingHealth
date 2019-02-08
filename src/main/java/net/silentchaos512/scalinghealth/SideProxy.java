package net.silentchaos512.scalinghealth;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.javafmlmod.FMLModLoadingContext;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultyAffected;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultySource;
import net.silentchaos512.scalinghealth.event.*;
import net.silentchaos512.scalinghealth.init.*;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;

class SideProxy {
    SideProxy() {
//        Config.load();
        ModLoot.init();
        
        FMLModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLModLoadingContext.get().getModEventBus().addListener(this::imcEnqueue);
        FMLModLoadingContext.get().getModEventBus().addListener(this::imcProcess);

        MinecraftForge.EVENT_BUS.addListener(ModBlocks::registerAll);
        MinecraftForge.EVENT_BUS.addListener(ModItems::registerAll);
        MinecraftForge.EVENT_BUS.addListener(ModEntities::registerAll);
        MinecraftForge.EVENT_BUS.addListener(ModSounds::registerAll);

        MinecraftForge.EVENT_BUS.register(new ScalingHealthCommonEvents());
        MinecraftForge.EVENT_BUS.register(new SHPlayerDataHandler.EventHandler());
        MinecraftForge.EVENT_BUS.register(DifficultyHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(BlightHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(PetEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(DamageScaling.INSTANCE);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        CapabilityDifficultyAffected.register();
        CapabilityDifficultySource.register();
        DifficultyHandler.INSTANCE.initPotionMap();
    }

    private void imcEnqueue(InterModEnqueueEvent event) { }

    private void imcProcess(InterModProcessEvent event) { }

    static class Client extends SideProxy {
        Client() {
            FMLModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

//            MinecraftForge.EVENT_BUS.register(HeartDisplayHandler.INSTANCE);
//            MinecraftForge.EVENT_BUS.register(DifficultyMeter.INSTANCE);
//            MinecraftForge.EVENT_BUS.register(KeyTrackerSH.INSTANCE);
        }

        private void clientSetup(FMLClientSetupEvent event) { }
    }

    static class Server extends SideProxy {
        Server() {
            FMLModLoadingContext.get().getModEventBus().addListener(this::serverSetup);
        }

        private void serverSetup(FMLDedicatedServerSetupEvent event) {
//            ModCommands.registerAll(event.getServerSupplier().get().getCommandManager().getDispatcher());
        }
    }
}
