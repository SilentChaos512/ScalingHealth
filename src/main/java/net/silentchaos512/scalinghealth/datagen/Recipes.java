package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.data.*;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.Tags;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.objects.Registration;

import java.util.function.Consumer;

public class Recipes extends RecipeProvider {
    public Recipes (DataGenerator gen) {
        super(gen);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        fullBlockRecipe(consumer, Registration.HEART_CRYSTAL.get(), Registration.HEART_CRYSTAL_SHARD.get());
        fullBlockRecipe(consumer, Registration.POWER_CRYSTAL.get(), Registration.POWER_CRYSTAL_SHARD.get());
        ShapelessRecipeBuilder.shapelessRecipe(Registration.HEART_DUST.get(), 24)
                .addIngredient(Registration.HEART_CRYSTAL.get())
                .addCriterion("cobblestone", getDefaultTrigger())
                .setGroup(ScalingHealth.MOD_ID)
                .build(consumer);

        ShapedRecipeBuilder.shapedRecipe(Registration.BANDAGES.get(), 2)
                .patternLine("xxx")
                .patternLine("yyy")
                .key('x', Items.PAPER)
                .key('y', Registration.HEART_DUST.get())
                .addCriterion("cobblestone", getDefaultTrigger())
                .setGroup(ScalingHealth.MOD_ID)
                .build(consumer);

        ShapedRecipeBuilder.shapedRecipe(Registration.MEDKIT.get(), 2)
                .patternLine("aba")
                .patternLine("cdc")
                .patternLine("eee")
                .key('a', Registration.HEART_DUST.get())
                .key('b', Tags.Items.INGOTS_IRON)
                .key('c', Registration.BANDAGES.get())
                .key('d', Items.GLISTERING_MELON_SLICE)
                .key('e', Items.TERRACOTTA)
                .addCriterion("cobblestone", getDefaultTrigger())
                .setGroup(ScalingHealth.MOD_ID)
                .build(consumer);
    }

    private void fullBlockRecipe(Consumer<IFinishedRecipe> consumer, IItemProvider result, IItemProvider ingredient){
        ShapedRecipeBuilder.shapedRecipe(result)
                .patternLine("xxx")
                .patternLine("xxx")
                .patternLine("xxx")
                .key('x', ingredient)
                .addCriterion("cobblestone", getDefaultTrigger())
                .setGroup(ScalingHealth.MOD_ID)
                .build(consumer);
    }

    private ICriterionInstance getDefaultTrigger(){
        return InventoryChangeTrigger.Instance.forItems(Items.COBBLESTONE);
    }
}
