package net.silentchaos512.scalinghealth.world;

import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silentchaos512.scalinghealth.ScalingHealth;

import static net.silentchaos512.scalinghealth.world.WorldObjectsRegistry.*;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public class SHWorldFeatures {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void addOres(BiomeLoadingEvent event) {
        event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, HEART_CRYSTAL_STONE);
        event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, POWER_CRYSTAL_STONE);
        event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, HEART_CRYSTAL_DEEPSLATE);
        event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, POWER_CRYSTAL_DEEPSLATE);
    }
}
