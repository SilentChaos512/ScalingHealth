package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.objects.Registration;

public class BlockStateGen extends BlockStateProvider {
    public BlockStateGen(GatherDataEvent event) {
        super(event.getGenerator(), ScalingHealth.MOD_ID, event.getExistingFileHelper());
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockAndBlockItem(Registration.HEART_CRYSTAL_ORE.get());
        simpleBlockAndBlockItem(Registration.POWER_CRYSTAL_ORE.get());

        simpleItem("bandages");
        simpleItem("chance_heart");
        simpleItem("cursed_heart");
        simpleItem("enchanted_heart");
        simpleItem("heart_crystal");
        simpleItem("heart_crystal_shard");
        simpleItem("heart_dust");
        simpleItem("medkit");
        simpleItem("power_crystal");
        simpleItem("power_crystal_shard");
    }

    private void simpleBlockAndBlockItem(Block block) {
        simpleBlock(block);
        simpleBlockItem(block, cubeAll(block));
    }

    private void simpleItem(String name) {
        itemModels().withExistingParent(name, "item/generated")
                .texture("layer0", ScalingHealth.getId("item/" + name));
    }
}
