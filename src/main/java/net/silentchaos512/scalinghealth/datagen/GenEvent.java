package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silentchaos512.scalinghealth.ScalingHealth;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID ,bus = Mod.EventBusSubscriber.Bus.MOD)
public class GenEvent {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        generator.addProvider(true, new Recipes(generator));
        generator.addProvider(true, new LootTablesGen(event));
        generator.addProvider(true, new SHEntityTagsProvider(event));
        generator.addProvider(true, new SHBlockTagsProvider(event));
        generator.addProvider(true, new LootModifierGen(event.getGenerator()));
        generator.addProvider(true, new EnglishLocalization(event.getGenerator()));
        generator.addProvider(true, new BlockStateGen(event));
        generator.addProvider(true, new WorldGenGenerator(event));
    }
}
