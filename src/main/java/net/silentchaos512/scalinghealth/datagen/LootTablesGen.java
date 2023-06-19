package net.silentchaos512.scalinghealth.datagen;

import com.google.common.collect.ImmutableList;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

public class LootTablesGen extends LootTableProvider {
    public static final List<SubProviderEntry> PROVIDERS = ImmutableList.of(
            new SubProviderEntry(BlockTables::new, LootContextParamSets.BLOCK),
            new SubProviderEntry(ChestLootTables::new, LootContextParamSets.CHEST),
            new SubProviderEntry(MobTables::new, LootContextParamSets.ENTITY)
    );

    public LootTablesGen(GatherDataEvent event) {
        super(event.getGenerator().getPackOutput(), Set.of(), PROVIDERS);
    }
}
