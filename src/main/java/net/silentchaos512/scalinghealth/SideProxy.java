package net.silentchaos512.scalinghealth;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultyAffected;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultySource;
import net.silentchaos512.scalinghealth.capability.CapabilityPlayerData;
import net.silentchaos512.scalinghealth.client.gui.DebugOverlay;
import net.silentchaos512.scalinghealth.client.gui.difficulty.DifficultyMeter;
import net.silentchaos512.scalinghealth.client.gui.health.HeartDisplayHandler;
import net.silentchaos512.scalinghealth.client.particles.ModParticles;
import net.silentchaos512.scalinghealth.command.ModCommands;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.event.DamageScaling;
import net.silentchaos512.scalinghealth.event.PetEventHandler;
import net.silentchaos512.scalinghealth.init.*;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.utils.gen.GenModels;
import net.silentchaos512.scalinghealth.utils.gen.GenRecipes;

import javax.annotation.Nullable;

public class SideProxy {
    SideProxy() {
        Config.init();
        Network.init();
        ModLoot.init();
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::imcEnqueue);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::imcProcess);
        MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModBlocks::registerAll);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModEntities::registerAll);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModItems::registerAll);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModPotions::registerAll);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModSounds::registerAll);
//        FMLModLoadingContext.get().getModEventBus().addListener(ModTileEntities::registerAll);

        MinecraftForge.EVENT_BUS.register(PetEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(DamageScaling.INSTANCE);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        CapabilityDifficultyAffected.register();
        CapabilityDifficultySource.register();
        CapabilityPlayerData.register();
    }

    void imcEnqueue(InterModEnqueueEvent event) {}

    private void imcProcess(InterModProcessEvent event) {
        if (ScalingHealth.RUN_GENERATORS && ScalingHealth.isDevBuild()) {
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

    @Nullable
    public EntityPlayer getClientPlayer() {
        return null;
    }

    static class Client extends SideProxy {
        Client() {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(Client::clientSetup);

            MinecraftForge.EVENT_BUS.register(HeartDisplayHandler.INSTANCE);
            MinecraftForge.EVENT_BUS.register(DifficultyMeter.INSTANCE);
//            MinecraftForge.EVENT_BUS.register(KeyTrackerSH.INSTANCE);

            DebugOverlay.init();
        }

        private static void clientSetup(FMLClientSetupEvent event) {
            ModEntities.registerRenderers(event);
        }

        @Override
        void imcEnqueue(InterModEnqueueEvent event) {
            super.imcEnqueue(event);
            // Not sure where this is supposed to go. Seems particle manager is created right
            // before IMC enqueue/process
            ModParticles.registerAll();
        }

        @Nullable
        @Override
        public EntityPlayer getClientPlayer() {
            return Minecraft.getInstance().player;
        }
    }

    static class Server extends SideProxy {
        Server() {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverSetup);
        }

        private void serverSetup(FMLDedicatedServerSetupEvent event) {}
    }
}
