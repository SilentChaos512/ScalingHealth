package net.silentchaos512.scalinghealth.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.objects.Registration;
import net.silentchaos512.scalinghealth.utils.EntityGroup;

import java.util.Locale;

public class EntityGroupCondition implements LootItemCondition {
   public static final ResourceLocation NAME = new ResourceLocation(ScalingHealth.MOD_ID, "entity_group_condition");

   private final EntityGroup group;

   public EntityGroupCondition(EntityGroup group) {
      this.group = group;
   }

   /**
    * Tests the mob group of this mob, and tests that a damage source is present.
    *
    * This is to prevent the associated modifier from being run if a creeper explodes for instance,
    * as it triggers the loot table of the blocks it explodes. In that case THIS_ENTITY
    * is present, but does not represent the loot table being triggered, this is problematic because the LootingLevelEvent
    * is then fired but with a null damage source, which most mods assume to be non null (correctly?)
    */
   @Override
   public boolean test(LootContext context) {
      Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
      return context.hasParam(LootContextParams.DAMAGE_SOURCE) && entity instanceof LivingEntity &&
              EntityGroup.from((LivingEntity) entity, true) == this.group;
   }

   @Override
   public LootItemConditionType getType() {
      return Registration.ENTITY_GROUP.get();
   }

   public static class ThisSerializer implements Serializer<EntityGroupCondition> {
      @Override
      public void serialize(JsonObject json, EntityGroupCondition condition, JsonSerializationContext context) {
         json.addProperty("entity_group", condition.group.toString().toLowerCase(Locale.ROOT));
      }

      @Override
      public EntityGroupCondition deserialize(JsonObject json, JsonDeserializationContext context) {
         String group = GsonHelper.getAsString(json, "entity_group");
         return new EntityGroupCondition(EntityGroup.from(group));
      }
   }
}
