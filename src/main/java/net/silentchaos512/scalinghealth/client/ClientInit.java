package net.silentchaos512.scalinghealth.client;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.client.gui.DebugOverlay;
import net.silentchaos512.scalinghealth.client.gui.difficulty.DifficultyMeter;
import net.silentchaos512.scalinghealth.client.gui.health.HeartDisplayHandler;
import net.silentchaos512.scalinghealth.objects.Registration;
import net.silentchaos512.utils.Color;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientInit {
    //A bit weird, but works.
    static {
        MinecraftForge.EVENT_BUS.register(HeartDisplayHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(DifficultyMeter.INSTANCE);

        DebugOverlay.init();
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        KeyManager.registerBindings();
    }

    @SubscribeEvent
    public static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
        ParticleEngine particles = Minecraft.getInstance().particleEngine;

        ImmutableMap.of(
                Registration.HEART_CRYSTAL_PARTICLE.get(), factory(Color.FIREBRICK),
                Registration.POWER_CRYSTAL_PARTICLE.get(), factory(Color.ROYALBLUE),
                Registration.CURSED_HEART_PARTICLE.get(), factory(Color.REBECCAPURPLE),
                Registration.ENCHANTED_HEART_PARTICLE.get(), factory(Color.ANTIQUEWHITE)
        ).forEach(particles::register);
    }

    private static ParticleProvider<SimpleParticleType> factory(Color color) {
        return new ColoredParticle.Factory(color);
    }
}
