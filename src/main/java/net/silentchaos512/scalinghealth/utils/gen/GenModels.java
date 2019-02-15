package net.silentchaos512.scalinghealth.utils.gen;

import net.silentchaos512.lib.util.generator.ModelGenerator;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.init.ModBlocks;
import net.silentchaos512.scalinghealth.init.ModItems;

public final class GenModels {
    private GenModels() {}

    public static void generate() {
        ScalingHealth.LOGGER.info("Generated blockstate and model files (this should only happen in dev builds!)");

        for (ModBlocks block : ModBlocks.values()) {
            ModelGenerator.create(block.asBlock());
        }

        for (ModItems item : ModItems.values()) {
            ModelGenerator.create(item.asItem());
        }
    }
}
