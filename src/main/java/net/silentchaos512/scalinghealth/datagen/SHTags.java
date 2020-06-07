package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.EntityTypeTagsProvider;
import net.silentchaos512.scalinghealth.tags.EntityTags;

import static net.minecraft.entity.EntityType.*;

public class SHTags extends EntityTypeTagsProvider {
   public SHTags(DataGenerator generator) {
      super(generator);
   }

   @Override
   protected void registerTags() {
      this.getBuilder(EntityTags.BLIGHT_EXEMPT).add(BAT, CAT, CHICKEN, COD, COW, DONKEY, FOX, HORSE, MOOSHROOM, MULE,
              OCELOT, PARROT, PIG, RABBIT, SALMON, SHEEP, TROPICAL_FISH, TURTLE, VILLAGER, WANDERING_TRADER);
      this.getBuilder(EntityTags.DIFFICULTY_EXEMPT).add(VILLAGER, WANDERING_TRADER);
   }
}
