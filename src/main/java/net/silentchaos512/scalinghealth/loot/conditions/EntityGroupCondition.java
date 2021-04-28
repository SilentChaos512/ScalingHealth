package net.silentchaos512.scalinghealth.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.utils.EntityGroup;

import java.util.Locale;

public class EntityGroupCondition implements ILootCondition {
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
      Entity entity = context.get(LootParameters.THIS_ENTITY);
      return context.has(LootParameters.DAMAGE_SOURCE) && entity instanceof LivingEntity &&
              EntityGroup.from((LivingEntity) entity, true) == this.group;
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
