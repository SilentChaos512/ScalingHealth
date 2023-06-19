package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.objects.Registration;

import java.util.function.Consumer;

public class Recipes extends RecipeProvider {
    public Recipes (DataGenerator gen) {
        super(gen.getPackOutput());
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        fullBlockRecipe(consumer, Registration.HEART_CRYSTAL.get(), Registration.HEART_CRYSTAL_SHARD.get());
        fullBlockRecipe(consumer, Registration.POWER_CRYSTAL.get(), Registration.POWER_CRYSTAL_SHARD.get());
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.HEART_DUST.get())
                .requires(Registration.HEART_CRYSTAL.get())
                .unlockedBy("cobblestone", getDefaultTrigger())
                .group(ScalingHealth.MOD_ID)
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Registration.BANDAGES.get())
                .pattern("xxx")
                .pattern("yyy")
                .define('x', Items.PAPER)
                .define('y', Registration.HEART_DUST.get())
                .unlockedBy("cobblestone", getDefaultTrigger())
                .group(ScalingHealth.MOD_ID)
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Registration.MEDKIT.get())
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
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
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
