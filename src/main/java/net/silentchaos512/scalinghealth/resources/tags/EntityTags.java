package net.silentchaos512.scalinghealth.resources.tags;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.silentchaos512.scalinghealth.ScalingHealth;

public class EntityTags {
   public static final TagKey<EntityType<?>> DIFFICULTY_EXEMPT = create(ScalingHealth.getId("difficulty_exempt"));
   public static final TagKey<EntityType<?>> BLIGHT_EXEMPT = create(ScalingHealth.getId("blight_exempt"));

   public static TagKey<EntityType<?>> create(ResourceLocation location) {
      return TagKey.create(Registries.ENTITY_TYPE, location);
   }
}
