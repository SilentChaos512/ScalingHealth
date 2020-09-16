package net.silentchaos512.scalinghealth.loot;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.silentchaos512.scalinghealth.item.DifficultyMutatorItem;
import net.silentchaos512.scalinghealth.item.PowerCrystal;
import net.silentchaos512.scalinghealth.utils.EnabledFeatures;

import javax.annotation.Nonnull;
import java.util.List;

public class TableGlobalModifier extends LootModifier{
   private final TableLootEntry table;

   protected TableGlobalModifier(ILootCondition[] conditions, TableLootEntry table) {
      super(conditions);
      this.table = table;
   }

   @Nonnull
   @Override
   protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
      table.func_216154_a(generatedLoot::add, context);
      generatedLoot.forEach(stack -> {
         if((EnabledFeatures.powerCrystalEnabled() && stack.getItem() instanceof PowerCrystal)
                 || (!EnabledFeatures.difficultyEnabled() && stack.getItem() instanceof DifficultyMutatorItem)){
            stack.setCount(0);
         }
      });
      return generatedLoot;
   }

   public static class Serializer extends GlobalLootModifierSerializer<TableGlobalModifier> {
      @Override
      public TableGlobalModifier read(ResourceLocation location, JsonObject object, ILootCondition[] lootConditions) {
         String resLoc = JSONUtils.getString(object, "table");
         TableLootEntry table = (TableLootEntry) TableLootEntry.builder(new ResourceLocation(resLoc)).build();
         return new TableGlobalModifier(lootConditions, table);
      }

      @Override
      public JsonObject write(TableGlobalModifier instance) {
         JsonObject json = makeConditions(instance.conditions);
//         json.addProperty("table", instance.table.table); TODO add AT
         return json;
      }
   }
}
