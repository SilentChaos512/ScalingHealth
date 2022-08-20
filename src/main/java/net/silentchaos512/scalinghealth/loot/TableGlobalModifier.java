package net.silentchaos512.scalinghealth.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.silentchaos512.scalinghealth.objects.item.DifficultyMutatorItem;
import net.silentchaos512.scalinghealth.objects.item.PowerCrystal;
import net.silentchaos512.scalinghealth.utils.config.EnabledFeatures;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class TableGlobalModifier extends LootModifier {
   public static final Supplier<Codec<TableGlobalModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst ->
           codecStart(inst).and(
                   ResourceLocation.CODEC.xmap(
                           r -> (LootTableReference) LootTableReference.lootTableReference(r).build(),
                           lt -> lt.name
                   ).fieldOf("table").forGetter(m -> m.table)
           ).apply(inst, TableGlobalModifier::new)));

   private final LootTableReference table;

   public TableGlobalModifier(LootItemCondition[] conditions, LootTableReference table) {
      super(conditions);
      this.table = table;
   }

   @Override
   public Codec<? extends IGlobalLootModifier> codec() {
      return CODEC.get();
   }

   @Nonnull
   @Override
   protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
      table.createItemStack(generatedLoot::add, context);
      generatedLoot.forEach(stack -> {
         if ((EnabledFeatures.powerCrystalEnabled() && stack.getItem() instanceof PowerCrystal)
                 || (!EnabledFeatures.difficultyEnabled() && stack.getItem() instanceof DifficultyMutatorItem)) {
            stack.setCount(0);
         }
      });
      return generatedLoot;
   }
}
