package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID ,bus = Mod.EventBusSubscriber.Bus.MOD)
public class GenEvent {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        generator.addProvider(new Recipes(generator));
        generator.addProvider(new LootTablesGenerator(generator));
        generator.addProvider(new SHEntityTagsProvider(event));
        generator.addProvider(new SHBlockTagsProvider(event));
        generator.addProvider(new LootModifierGen(event.getGenerator()));
        generator.addProvider(new EnglishLocalization(event.getGenerator()));
        generator.addProvider(new BlockStateGen(event));
    }
}
