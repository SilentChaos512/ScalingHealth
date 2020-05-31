package net.silentchaos512.scalinghealth.tags;

import net.minecraft.entity.EntityType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.Tag;
import net.silentchaos512.scalinghealth.ScalingHealth;

public class EntityTags {
   public static final Tag<EntityType<?>> DIFFICULTY_EXEMPT = new EntityTypeTags.Wrapper(ScalingHealth.getId("difficulty_exempt"));
   public static final Tag<EntityType<?>> BLIGHT_EXEMPT = new EntityTypeTags.Wrapper(ScalingHealth.getId("blight_exempt"));
}
