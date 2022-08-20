package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.loot.TableGlobalModifier;
import net.silentchaos512.scalinghealth.loot.conditions.EntityGroupCondition;
import net.silentchaos512.scalinghealth.utils.EntityGroup;

public class LootModifierGen extends GlobalLootModifierProvider {
    public LootModifierGen(DataGenerator gen) {
        super(gen, ScalingHealth.MOD_ID);
    }

    @Override
    protected void start() {
//        this.add("boss", tableGlobalModifier,
//                new TableGlobalModifier(
//                        new ILootCondition[]{new EntityGroupCondition(EntityGroup.BOSS)},
//                        (TableLootEntry) TableLootEntry.builder(ScalingHealth.getId("bonus_drops/boss")).build()
//                ));
        this.add("hostile",
                new TableGlobalModifier(
                        new LootItemCondition[]{new EntityGroupCondition(EntityGroup.HOSTILE)},
                        (LootTableReference) LootTableReference.lootTableReference(ScalingHealth.getId("bonus_drops/hostile")).build()
                ));
        this.add("peaceful",
                new TableGlobalModifier(
                        new LootItemCondition[]{new EntityGroupCondition(EntityGroup.PEACEFUL)},
                        (LootTableReference) LootTableReference.lootTableReference(ScalingHealth.getId("bonus_drops/peaceful")).build()
                ));

        LootTablesGenerator.CHESTS.forEach(rl ->
                this.add(rl.getPath(),
                        new TableGlobalModifier(
                                new LootItemCondition[]{LootTableIdCondition.builder(rl).build()},
                                (LootTableReference) LootTableReference.lootTableReference(LootTablesGenerator.VANILLA_TO_SH.get(rl)).build()
                        )
                ));

    }
}
