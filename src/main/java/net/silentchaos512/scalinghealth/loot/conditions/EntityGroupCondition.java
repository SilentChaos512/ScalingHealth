package net.silentchaos512.scalinghealth.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.lib.EntityGroup;

public class EntityGroupCondition implements ILootCondition {
   public static final Serializer SERIALIZER = new Serializer();

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

   public static class Serializer extends AbstractSerializer<EntityGroupCondition> {
      protected Serializer() {
         super(ScalingHealth.getId("entity_group_condition"), EntityGroupCondition.class);
      }

      @Override
      public void serialize(JsonObject json, EntityGroupCondition condition, JsonSerializationContext context) {
         json.addProperty("entity_group", condition.group.toString());
      }

      @Override
      public EntityGroupCondition deserialize(JsonObject json, JsonDeserializationContext context) {
         String group = JSONUtils.getString(json, "entity_group");
         return new EntityGroupCondition(EntityGroup.from(group));
      }
   }
}
