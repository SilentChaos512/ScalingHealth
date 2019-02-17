package net.silentchaos512.scalinghealth;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLModLoadingContext;
import net.silentchaos512.lib.util.GameUtil;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultyAffected;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultySource;
import net.silentchaos512.scalinghealth.capability.CapabilityPlayerData;
import net.silentchaos512.scalinghealth.client.gui.DebugOverlay;
import net.silentchaos512.scalinghealth.client.gui.difficulty.DifficultyMeter;
import net.silentchaos512.scalinghealth.client.gui.health.HeartDisplayHandler;
import net.silentchaos512.scalinghealth.command.ModCommands;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.event.BlightHandler;
import net.silentchaos512.scalinghealth.event.DamageScaling;
import net.silentchaos512.scalinghealth.event.PetEventHandler;
import net.silentchaos512.scalinghealth.init.*;
import net.silentchaos512.scalinghealth.utils.gen.GenModels;
import net.silentchaos512.scalinghealth.utils.gen.GenRecipes;

class SideProxy {
    SideProxy() {
        Config.init();
        ModLoot.init();
        
        FMLModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLModLoadingContext.get().getModEventBus().addListener(this::imcEnqueue);
        FMLModLoadingContext.get().getModEventBus().addListener(this::imcProcess);
        MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);

        FMLModLoadingContext.get().getModEventBus().addListener(ModBlocks::registerAll);
        FMLModLoadingContext.get().getModEventBus().addListener(ModEntities::registerAll);
        FMLModLoadingContext.get().getModEventBus().addListener(ModItems::registerAll);
        FMLModLoadingContext.get().getModEventBus().addListener(ModPotions::registerAll);
        FMLModLoadingContext.get().getModEventBus().addListener(ModSounds::registerAll);
//        FMLModLoadingContext.get().getModEventBus().addListener(ModTileEntities::registerAll);

        MinecraftForge.EVENT_BUS.register(BlightHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(PetEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(DamageScaling.INSTANCE);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        CapabilityDifficultyAffected.register();
        CapabilityDifficultySource.register();
        CapabilityPlayerData.register();
    }

    private void imcEnqueue(InterModEnqueueEvent event) { }

    private void imcProcess(InterModProcessEvent event) {
        if (GameUtil.isDeobfuscated()) {
            GenModels.generate();
            GenRecipes.generate();
        }
    }

    private void serverAboutToStart(FMLServerAboutToStartEvent event) {
        ModCommands.registerAll(event.getServer().getCommandManager().getDispatcher());
    }

    private void serverStarted(FMLServerStartedEvent event) {
        ModGameRules.registerAll(event.getServer());
    }

    static class Client extends SideProxy {
        Client() {
            FMLModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

            MinecraftForge.EVENT_BUS.register(HeartDisplayHandler.INSTANCE);
            MinecraftForge.EVENT_BUS.register(DifficultyMeter.INSTANCE);
//            MinecraftForge.EVENT_BUS.register(KeyTrackerSH.INSTANCE);

            DebugOverlay.init();
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
