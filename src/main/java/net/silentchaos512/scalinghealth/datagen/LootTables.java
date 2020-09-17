package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.data.DataGenerator;
import net.silentchaos512.scalinghealth.init.ModBlocks;
import net.silentchaos512.scalinghealth.init.ModItems;
import net.silentchaos512.scalinghealth.lib.EntityGroup;

public class LootTables extends BaseLootTable {
    public LootTables(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void addTables() {
        lootTables.put(ModBlocks.HEART_CRYSTAL_ORE.asBlock(), createSilkTouchTable("heart_crystal_ore", ModBlocks.HEART_CRYSTAL_ORE.asBlock(), ModItems.HEART_CRYSTAL_SHARD.asItem()));
        lootTables.put(ModBlocks.POWER_CRYSTAL_ORE.asBlock(), createSilkTouchTable("power_crystal_ore", ModBlocks.POWER_CRYSTAL_ORE.asBlock(), ModItems.POWER_CRYSTAL_SHARD.asItem()));
        mobLootTable.put(EntityGroup.HOSTILE,
                createSHDropsTable(
                        createSHDropsPool("crystals", 1,
                                new MobLootCondition(true, false, 0, 0.055f, 0.005f),
                                new MobLootEntry(ModItems.HEART_CRYSTAL.asItem(), 12, 1, 0),
                                new MobLootEntry(ModItems.POWER_CRYSTAL.asItem(), 4, 1, 0),
                                new MobLootEntry(ModItems.HEART_CRYSTAL_SHARD.asItem(), 2, 11, 3),
                                new MobLootEntry(ModItems.POWER_CRYSTAL_SHARD.asItem(), 1, 12, 2)),
                        createSHDropsPool("extras", 1,
                                new MobLootCondition(false, false, 10, 0.025f, 0.005f),
                                new MobLootEntry(ModItems.BANDAGES.asItem(), 10, 2, 0),
                                new MobLootEntry(ModItems.MEDKIT.asItem(), 1, 1, 0)),
                        createSHDropsPool("difficulty_mutators", 1,
                                new MobLootCondition(false, false, 70, 0.015f, 0.025f),
                                new MobLootEntry(ModItems.CHANCE_HEART.asItem(), 1, 1, 0),
                                new MobLootEntry(ModItems.ENCHANTED_HEART.asItem(), 1, 1, 0)),
                        createSHDropsPool("blights", 1,
                                new MobLootCondition(true, true, 0, 0, 0),
                                new MobLootEntry(ModItems.HEART_CRYSTAL.asItem(), 10, 3, 1),
                                new MobLootEntry(ModItems.POWER_CRYSTAL.asItem(), 5, 2, 1))
                ));
        mobLootTable.put(EntityGroup.BOSS,
                createSHDropsTable(
                        createSHDropsPool("crystals", 3,
                                new MobLootCondition(true, false, 0, 0.055f, 0.005f),
                                new MobLootEntry(ModItems.HEART_CRYSTAL.asItem(), 12, 4, 2),
                                new MobLootEntry(ModItems.POWER_CRYSTAL.asItem(), 6, 2, 1),
                                new MobLootEntry(ModItems.HEART_CRYSTAL_SHARD.asItem(), 2, 40, 12),
                                new MobLootEntry(ModItems.POWER_CRYSTAL_SHARD.asItem(), 1, 32, 10)),
                        createSHDropsPool("difficulty_mutators", 1,
                                new MobLootCondition(false, false, 125, 0.5f, 0.01f),
                                new MobLootEntry(ModItems.CURSED_HEART.asItem(), 1, 2, 0),
                                new MobLootEntry(ModItems.CHANCE_HEART.asItem(), 2, 2, 0),
                                new MobLootEntry(ModItems.ENCHANTED_HEART.asItem(), 1, 2, 0)),
                        createSHDropsPool("blights", 1,
                                new MobLootCondition(true, true, 0, 0, 0),
                                new MobLootEntry(ModItems.HEART_CRYSTAL.asItem(), 10, 3, 1),
                                new MobLootEntry(ModItems.POWER_CRYSTAL.asItem(), 5, 2, 1))
                ));
        mobLootTable.put(EntityGroup.PEACEFUL,
                createSHDropsTable(
                        createSHDropsPool("crystals", 1,
                                new MobLootCondition(true, false, 0, 0.055f, 0.005f),
                                new MobLootEntry(ModItems.HEART_CRYSTAL_SHARD.asItem(), 2, 8, 1),
                                new MobLootEntry(ModItems.POWER_CRYSTAL_SHARD.asItem(), 1, 5, 0)),
                        createSHDropsPool("difficulty_mutators", 1,
                                new MobLootCondition(false, false, 0, 0.015f, 0.025f),
                                new MobLootEntry(ModItems.CURSED_HEART.asItem(), 1, 1, 0)),
                        createSHDropsPool("blights", 1,
                                new MobLootCondition(true, true, 0, 0, 0),
                                new MobLootEntry(ModItems.HEART_CRYSTAL.asItem(), 5, 1, -1),
                                new MobLootEntry(ModItems.POWER_CRYSTAL.asItem(), 2, 1, -1))
                ));
    }
}