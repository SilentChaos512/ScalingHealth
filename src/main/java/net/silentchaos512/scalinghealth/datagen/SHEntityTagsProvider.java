package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.resources.tags.EntityTags;

import static net.minecraft.world.entity.EntityType.*;

public class SHEntityTagsProvider extends EntityTypeTagsProvider {
   public SHEntityTagsProvider(GatherDataEvent event) {
      super(event.getGenerator().getPackOutput(), event.getLookupProvider(), ScalingHealth.MOD_ID, event.getExistingFileHelper());
   }

   @Override
   protected void addTags(HolderLookup.Provider provider) {
      this.tag(EntityTags.BLIGHT_EXEMPT).add(BAT, CAT, CHICKEN, COD, COW, DONKEY, FOX, HORSE, MOOSHROOM, MULE,
              OCELOT, PARROT, PIG, RABBIT, SALMON, SHEEP, TROPICAL_FISH, TURTLE, VILLAGER, WANDERING_TRADER);
      this.tag(EntityTags.DIFFICULTY_EXEMPT).add(VILLAGER, WANDERING_TRADER);
   }
}
