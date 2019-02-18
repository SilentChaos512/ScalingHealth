package net.silentchaos512.scalinghealth.utils.gen;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.silentchaos512.lib.util.generator.RecipeGenerator;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.init.ModBlocks;
import net.silentchaos512.scalinghealth.init.ModItems;

public final class GenRecipes {
    private GenRecipes() {}

    public static void generate() {
        ScalingHealth.LOGGER.info("Generated recipe files (this should only happen in dev builds!)");

        RecipeGenerator.create(id("bandages"), RecipeGenerator.ShapedBuilder
                .create(ModItems.BANDAGES, 2)
                .layout("ppp", "ddd")
                .key('p', Items.PAPER)
                .key('d', ModItems.HEART_DUST));
        RecipeGenerator.create(id("heart_crystal"), RecipeGenerator.ShapedBuilder
                .create(ModItems.HEART_CRYSTAL)
                .layout("###", "###", "###")
                .key('#', ModItems.HEART_CRYSTAL_SHARD));
        RecipeGenerator.create(id("heart_dust"), RecipeGenerator.ShapelessBuilder
                .create(ModItems.HEART_DUST, 24)
                .ingredient(ModItems.HEART_CRYSTAL));
        RecipeGenerator.create(id("medkit"), RecipeGenerator.ShapedBuilder
                .create(ModItems.MEDKIT, 2)
                .layout("did", "bpb", "ttt")
                .key('d', ModItems.HEART_DUST)
                .key('i', Tags.Items.INGOTS_IRON)
                .key('b', ModItems.BANDAGES)
                .key('p', Items.GLISTERING_MELON_SLICE)
                .key('t', Blocks.TERRACOTTA));
        RecipeGenerator.create(id("power_crystal"), RecipeGenerator.ShapedBuilder
                .create(ModItems.POWER_CRYSTAL)
                .layout("###", "###", "###")
                .key('#', ModItems.POWER_CRYSTAL_SHARD));

        RecipeGenerator.create(id("heart_crystal_ore_smelting"), RecipeGenerator.SmeltingBuilder
                .create(ModItems.HEART_CRYSTAL_SHARD)
                .ingredient(ModBlocks.HEART_CRYSTAL_ORE)
                .experience(1.0f));
        RecipeGenerator.create(id("power_crystal_ore_smelting"), RecipeGenerator.SmeltingBuilder
                .create(ModItems.POWER_CRYSTAL_SHARD)
                .ingredient(ModBlocks.POWER_CRYSTAL_ORE)
                .experience(1.0f));
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(ScalingHealth.MOD_ID, path);
    }
}
