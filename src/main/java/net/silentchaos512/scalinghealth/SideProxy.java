package net.silentchaos512.scalinghealth;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.silentchaos512.scalinghealth.capability.DifficultyAffectedCapability;
import net.silentchaos512.scalinghealth.capability.DifficultySourceCapability;
import net.silentchaos512.scalinghealth.capability.PetHealthCapability;
import net.silentchaos512.scalinghealth.capability.PlayerDataCapability;
import net.silentchaos512.scalinghealth.client.KeyBinds.KeyManager;
import net.silentchaos512.scalinghealth.client.gui.DebugOverlay;
import net.silentchaos512.scalinghealth.client.gui.difficulty.DifficultyMeter;
import net.silentchaos512.scalinghealth.client.gui.health.HeartDisplayHandler;
import net.silentchaos512.scalinghealth.command.ModCommands;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.event.DamageScaling;
import net.silentchaos512.scalinghealth.event.PetEventHandler;
import net.silentchaos512.scalinghealth.init.*;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.world.SHWorldFeatures;

import javax.annotation.Nullable;

public class SideProxy {
    SideProxy() {
        Config.init();
        Network.init();
        ModLoot.init();
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);

        MinecraftForge.EVENT_BUS.register(PetEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(DamageScaling.INSTANCE);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        DifficultyAffectedCapability.register();
        DifficultySourceCapability.register();
        PlayerDataCapability.register();
        PetHealthCapability.register();
        DeferredWorkQueue.runLater(SHWorldFeatures::addFeaturesToBiomes);
    }

    private void serverAboutToStart(FMLServerAboutToStartEvent event) {
        ModCommands.registerAll(event.getServer().getCommandManager().getDispatcher());
    }

    @Nullable
    public PlayerEntity getClientPlayer() {
        return null;
    }

    static class Client extends SideProxy {
        Client() {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(Client::clientSetup);

            MinecraftForge.EVENT_BUS.register(HeartDisplayHandler.INSTANCE);
            MinecraftForge.EVENT_BUS.register(DifficultyMeter.INSTANCE);

            DebugOverlay.init();
        }

        private static void clientSetup(FMLClientSetupEvent event) {
            KeyManager.registerBindings();
        }

        @Nullable
        @Override
        public PlayerEntity getClientPlayer() {
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
