package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootTableIdCondition;
import net.minecraftforge.registries.ObjectHolder;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.loot.TableGlobalModifier;
import net.silentchaos512.scalinghealth.loot.conditions.EntityGroupCondition;
import net.silentchaos512.scalinghealth.utils.EntityGroup;

public class LootModifierGen extends GlobalLootModifierProvider {
    @ObjectHolder("scalinghealth:table_loot_mod")
    public static final GlobalLootModifierSerializer<TableGlobalModifier> tableGlobalModifier = null;

    public LootModifierGen(DataGenerator gen) {
        super(gen, ScalingHealth.MOD_ID);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    @SuppressWarnings("ConstantConditions")
    protected void start() {
//        this.add("boss", tableGlobalModifier,
//                new TableGlobalModifier(
//                        new ILootCondition[]{new EntityGroupCondition(EntityGroup.BOSS)},
//                        (TableLootEntry) TableLootEntry.builder(ScalingHealth.getId("bonus_drops/boss")).build()
//                ));
        this.add("hostile", tableGlobalModifier,
                new TableGlobalModifier(
                        new ILootCondition[]{new EntityGroupCondition(EntityGroup.HOSTILE)},
                        (TableLootEntry) TableLootEntry.builder(ScalingHealth.getId("bonus_drops/hostile")).build()
                ));
        this.add("peaceful", tableGlobalModifier,
                new TableGlobalModifier(
                        new ILootCondition[]{new EntityGroupCondition(EntityGroup.PEACEFUL)},
                        (TableLootEntry) TableLootEntry.builder(ScalingHealth.getId("bonus_drops/peaceful")).build()
                ));

        LootTablesGenerator.CHESTS.forEach(rl ->
                this.add(rl.getPath(), tableGlobalModifier,
                        new TableGlobalModifier(
                                new ILootCondition[]{LootTableIdCondition.builder(rl).build()},
                                (TableLootEntry) TableLootEntry.builder(LootTablesGenerator.VANILLA_TO_SH.get(rl)).build()
                        )
                ));

    }
}
