package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.resources.tags.EntityTags;

import static net.minecraft.world.entity.EntityType.*;

public class SHEntityTags extends EntityTypeTagsProvider {
   public SHEntityTags(GatherDataEvent event) {
      super(event.getGenerator(), ScalingHealth.MOD_ID, event.getExistingFileHelper());
   }

   @Override
   protected void addTags() {
      this.tag(EntityTags.BLIGHT_EXEMPT).add(BAT, CAT, CHICKEN, COD, COW, DONKEY, FOX, HORSE, MOOSHROOM, MULE,
              OCELOT, PARROT, PIG, RABBIT, SALMON, SHEEP, TROPICAL_FISH, TURTLE, VILLAGER, WANDERING_TRADER);
      this.tag(EntityTags.DIFFICULTY_EXEMPT).add(VILLAGER, WANDERING_TRADER);
   }
}
