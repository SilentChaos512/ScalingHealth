package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.objects.Registration;

public class SHBlockTagsProvider extends BlockTagsProvider {
    public SHBlockTagsProvider(GatherDataEvent event) {
        super(event.getGenerator(), ScalingHealth.MOD_ID, event.getExistingFileHelper());
    }

    @Override
    protected void addTags() {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(Registration.HEART_CRYSTAL_ORE.get())
                .add(Registration.POWER_CRYSTAL_ORE.get());

        this.tag(BlockTags.NEEDS_IRON_TOOL)
                .add(Registration.HEART_CRYSTAL_ORE.get())
                .add(Registration.POWER_CRYSTAL_ORE.get());
    }
}
