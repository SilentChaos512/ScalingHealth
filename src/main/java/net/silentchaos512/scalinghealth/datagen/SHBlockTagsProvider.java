package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.objects.Registration;

public class SHBlockTagsProvider extends BlockTagsProvider {
    public SHBlockTagsProvider(GatherDataEvent event) {
        super(event.getGenerator().getPackOutput(), event.getLookupProvider(),  ScalingHealth.MOD_ID, event.getExistingFileHelper());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(Registration.HEART_CRYSTAL_ORE.get())
                .add(Registration.POWER_CRYSTAL_ORE.get())
                .add(Registration.DEEPLSATE_HEART_CRYSTAL_ORE.get())
                .add(Registration.DEEPSLATE_POWER_CRYSTAL_ORE.get());

        this.tag(BlockTags.NEEDS_IRON_TOOL)
                .add(Registration.HEART_CRYSTAL_ORE.get())
                .add(Registration.POWER_CRYSTAL_ORE.get())
                .add(Registration.DEEPLSATE_HEART_CRYSTAL_ORE.get())
                .add(Registration.DEEPSLATE_POWER_CRYSTAL_ORE.get());
    }
}
