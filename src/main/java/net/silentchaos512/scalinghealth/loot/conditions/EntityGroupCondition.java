package net.silentchaos512.scalinghealth.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.util.JSONUtils;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.lib.EntityGroup;

import java.util.Locale;

public class EntityGroupCondition implements ILootCondition {
   public static final ResourceLocation NAME = new ResourceLocation(ScalingHealth.MOD_ID, "entity_group_condition");

   private final EntityGroup group;

   public EntityGroupCondition(EntityGroup group){
      this.group = group;
   }

   @Override
   public boolean test(LootContext context) {
      Entity entity = context.get(LootParameters.THIS_ENTITY);
      if(entity instanceof LivingEntity){
         return EntityGroup.from((LivingEntity) entity, true) == this.group;
      }
      return false;
   }

   @Override
   public LootConditionType getConditionType() {
      return Registry.LOOT_CONDITION_TYPE.getOptional(NAME)
              .orElseThrow(() -> new RuntimeException("Loot condition type did not register for some reason"));
   }

   public static class Serializer implements ILootSerializer<EntityGroupCondition> {
      @Override
      public void serialize(JsonObject json, EntityGroupCondition condition, JsonSerializationContext context) {
         json.addProperty("entity_group", condition.group.toString().toLowerCase(Locale.ROOT));
      }

      @Override
      public EntityGroupCondition deserialize(JsonObject json, JsonDeserializationContext context) {
         String group = JSONUtils.getString(json, "entity_group");
         return new EntityGroupCondition(EntityGroup.from(group));
      }
   }
}
