package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.data.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.Tags;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.init.ModItems;

import java.util.function.Consumer;

public class Recipes extends RecipeProvider {
    public Recipes (DataGenerator gen) {
        super(gen);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        fullBlockRecipe(consumer, ModItems.HEART_CRYSTAL, ModItems.HEART_CRYSTAL_SHARD);
        fullBlockRecipe(consumer, ModItems.POWER_CRYSTAL, ModItems.POWER_CRYSTAL_SHARD);
        ShapelessRecipeBuilder.shapelessRecipe(ModItems.HEART_DUST, 24)
                .addIngredient(ModItems.HEART_CRYSTAL)
                .addCriterion("cobblestone", getDefaultTrigger())
                .setGroup(ScalingHealth.MOD_ID)
                .build(consumer);

        ShapedRecipeBuilder.shapedRecipe(ModItems.BANDAGES, 2)
                .patternLine("xxx")
                .patternLine("yyy")
                .key('x', Items.PAPER)
                .key('y', ModItems.HEART_DUST)
                .addCriterion("cobblestone", getDefaultTrigger())
                .setGroup(ScalingHealth.MOD_ID)
                .build(consumer);

        ShapedRecipeBuilder.shapedRecipe(ModItems.MEDKIT, 2)
                .patternLine("aba")
                .patternLine("cdc")
                .patternLine("eee")
                .key('a', ModItems.HEART_DUST)
                .key('b', Tags.Items.INGOTS_IRON)
                .key('c', ModItems.BANDAGES)
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
