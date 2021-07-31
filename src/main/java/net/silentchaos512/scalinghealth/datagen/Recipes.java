package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.objects.Registration;

import java.util.function.Consumer;

public class Recipes extends RecipeProvider {
    public Recipes (DataGenerator gen) {
        super(gen);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        fullBlockRecipe(consumer, Registration.HEART_CRYSTAL.get(), Registration.HEART_CRYSTAL_SHARD.get());
        fullBlockRecipe(consumer, Registration.POWER_CRYSTAL.get(), Registration.POWER_CRYSTAL_SHARD.get());
        ShapelessRecipeBuilder.shapeless(Registration.HEART_DUST.get(), 24)
                .requires(Registration.HEART_CRYSTAL.get())
                .unlockedBy("cobblestone", getDefaultTrigger())
                .group(ScalingHealth.MOD_ID)
                .save(consumer);

        ShapedRecipeBuilder.shaped(Registration.BANDAGES.get(), 2)
                .pattern("xxx")
                .pattern("yyy")
                .define('x', Items.PAPER)
                .define('y', Registration.HEART_DUST.get())
                .unlockedBy("cobblestone", getDefaultTrigger())
                .group(ScalingHealth.MOD_ID)
                .save(consumer);

        ShapedRecipeBuilder.shaped(Registration.MEDKIT.get(), 2)
                .pattern("aba")
                .pattern("cdc")
                .pattern("eee")
                .define('a', Registration.HEART_DUST.get())
                .define('b', Tags.Items.INGOTS_IRON)
                .define('c', Registration.BANDAGES.get())
                .define('d', Items.GLISTERING_MELON_SLICE)
                .define('e', Items.TERRACOTTA)
                .unlockedBy("cobblestone", getDefaultTrigger())
                .group(ScalingHealth.MOD_ID)
                .save(consumer);
    }

    private void fullBlockRecipe(Consumer<FinishedRecipe> consumer, ItemLike result, ItemLike ingredient){
        ShapedRecipeBuilder.shaped(result)
                .pattern("xxx")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', ingredient)
                .unlockedBy("cobblestone", getDefaultTrigger())
                .group(ScalingHealth.MOD_ID)
                .save(consumer);
    }

    private CriterionTriggerInstance getDefaultTrigger(){
        return InventoryChangeTrigger.TriggerInstance.hasItems(Items.COBBLESTONE);
    }
}
