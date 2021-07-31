package net.silentchaos512.scalinghealth.resources.tags;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityType;
import net.silentchaos512.scalinghealth.ScalingHealth;

public class EntityTags {
   public static final Tag.Named<EntityType<?>> DIFFICULTY_EXEMPT =
           EntityTypeTags.bind(ScalingHealth.getId("difficulty_exempt").toString());
   public static final Tag.Named<EntityType<?>> BLIGHT_EXEMPT =
           EntityTypeTags.bind(ScalingHealth.getId("blight_exempt").toString());
}
