package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.data.DataGenerator;
import net.silentchaos512.scalinghealth.utils.EntityGroup;

import static net.silentchaos512.scalinghealth.objects.Registration.*;

public class LootTablesGenerator extends BaseLootTableGenerator {
    public LootTablesGenerator(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void addTables() {
        lootTables.put(HEART_CRYSTAL_ORE.get(), createSilkTouchTable("heart_crystal_ore", HEART_CRYSTAL_ORE.get(), HEART_CRYSTAL_SHARD.get()));
        lootTables.put(POWER_CRYSTAL_ORE.get(), createSilkTouchTable("power_crystal_ore", POWER_CRYSTAL_ORE.get(), POWER_CRYSTAL_SHARD.get()));
        mobLootTable.put(EntityGroup.HOSTILE,
                createSHDropsTable(
                        createSHDropsPool("crystals", 1,
                                new MobLootCondition(true, false, 0, 0.055f, 0.005f),
                                new MobLootEntry(HEART_CRYSTAL.get(), 12, 1, 0),
                                new MobLootEntry(POWER_CRYSTAL.get(), 4, 1, 0),
                                new MobLootEntry(HEART_CRYSTAL_SHARD.get(), 2, 11, 3),
                                new MobLootEntry(POWER_CRYSTAL_SHARD.get(), 1, 12, 2)),
                        createSHDropsPool("extras", 1,
                                new MobLootCondition(false, false, 10, 0.025f, 0.005f),
                                new MobLootEntry(BANDAGES.get(), 10, 2, 0),
                                new MobLootEntry(MEDKIT.get(), 1, 1, 0)),
                        createSHDropsPool("difficulty_mutators", 1,
                                new MobLootCondition(false, false, 70, 0.015f, 0.025f),
                                new MobLootEntry(CHANCE_HEART.get(), 1, 1, 0),
                                new MobLootEntry(ENCHANTED_HEART.get(), 1, 1, 0)),
                        createSHDropsPool("blights", 1,
                                new MobLootCondition(true, true, 0, 0, 0),
                                new MobLootEntry(HEART_CRYSTAL.get(), 10, 3, 1),
                                new MobLootEntry(POWER_CRYSTAL.get(), 5, 2, 1))
                ));
//        mobLootTable.put(EntityGroup.BOSS,
//                createSHDropsTable(
//                        createSHDropsPool("crystals", 3,
//                                new MobLootCondition(true, false, 0, 0.055f, 0.005f),
//                                new MobLootEntry(HEART_CRYSTAL.get(), 12, 4, 2),
//                                new MobLootEntry(POWER_CRYSTAL.get(), 6, 2, 1),
//                                new MobLootEntry(HEART_CRYSTAL_SHARD.get(), 2, 40, 12),
//                                new MobLootEntry(POWER_CRYSTAL_SHARD.get(), 1, 32, 10)),
//                        createSHDropsPool("difficulty_mutators", 1,
//                                new MobLootCondition(false, false, 125, 0.5f, 0.01f),
//                                new MobLootEntry(CURSED_HEART.get(), 1, 2, 0),
//                                new MobLootEntry(CHANCE_HEART.get(), 2, 2, 0),
//                                new MobLootEntry(ENCHANTED_HEART.get(), 1, 2, 0)),
//                        createSHDropsPool("blights", 1,
//                                new MobLootCondition(true, true, 0, 0, 0),
//                                new MobLootEntry(HEART_CRYSTAL.get(), 10, 3, 1),
//                                new MobLootEntry(POWER_CRYSTAL.get(), 5, 2, 1))
//                ));
        mobLootTable.put(EntityGroup.PEACEFUL,
                createSHDropsTable(
                        createSHDropsPool("crystals", 1,
                                new MobLootCondition(true, false, 0, 0.055f, 0.005f),
                                new MobLootEntry(HEART_CRYSTAL_SHARD.get(), 2, 8, 1),
                                new MobLootEntry(POWER_CRYSTAL_SHARD.get(), 1, 5, 0)),
                        createSHDropsPool("difficulty_mutators", 1,
                                new MobLootCondition(false, false, 0, 0.015f, 0.025f),
                                new MobLootEntry(CURSED_HEART.get(), 1, 1, 0)),
                        createSHDropsPool("blights", 1,
                                new MobLootCondition(true, true, 0, 0, 0),
                                new MobLootEntry(HEART_CRYSTAL.get(), 5, 1, -1),
                                new MobLootEntry(POWER_CRYSTAL.get(), 2, 1, -1))
                ));
    }
}