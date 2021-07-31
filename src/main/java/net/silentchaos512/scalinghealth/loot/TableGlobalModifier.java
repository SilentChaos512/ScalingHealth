package net.silentchaos512.scalinghealth.loot;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.silentchaos512.scalinghealth.objects.item.DifficultyMutatorItem;
import net.silentchaos512.scalinghealth.objects.item.PowerCrystal;
import net.silentchaos512.scalinghealth.utils.config.EnabledFeatures;

import javax.annotation.Nonnull;
import java.util.List;

public class TableGlobalModifier extends LootModifier{
   private final LootTableReference table;

   public TableGlobalModifier(LootItemCondition[] conditions, LootTableReference table) {
      super(conditions);
      this.table = table;
   }

   @Nonnull
   @Override
   protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
      table.createItemStack(generatedLoot::add, context);
      generatedLoot.forEach(stack -> {
         if ((EnabledFeatures.powerCrystalEnabled() && stack.getItem() instanceof PowerCrystal)
                 || (!EnabledFeatures.difficultyEnabled() && stack.getItem() instanceof DifficultyMutatorItem)) {
            stack.setCount(0);
         }
      });
      return generatedLoot;
   }

   public static class Serializer extends GlobalLootModifierSerializer<TableGlobalModifier> {
      @Override
      public TableGlobalModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] lootConditions) {
         String resLoc = GsonHelper.getAsString(object, "table");
         LootTableReference table = (LootTableReference) LootTableReference.lootTableReference(new ResourceLocation(resLoc)).build();
         return new TableGlobalModifier(lootConditions, table);
      }

      @Override
      public JsonObject write(TableGlobalModifier instance) {
         JsonObject json = makeConditions(instance.conditions);
         json.addProperty("table", instance.table.name.toString());
         return json;
      }
   }
}
