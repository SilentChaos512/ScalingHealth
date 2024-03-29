package net.silentchaos512.scalinghealth.datagen;

import com.google.common.collect.Lists;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.objects.Registration;
import net.silentchaos512.scalinghealth.utils.EntityGroup;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LootTablesGenerator extends BaseLootTableGenerator {
    public LootTablesGenerator(DataGenerator generator) {
        super(generator);
    }

    public static final List<ResourceLocation> CHESTS = Lists.newArrayList(
            BuiltInLootTables.ABANDONED_MINESHAFT,
            BuiltInLootTables.BURIED_TREASURE,
            BuiltInLootTables.NETHER_BRIDGE,
            BuiltInLootTables.SIMPLE_DUNGEON,
            BuiltInLootTables.STRONGHOLD_LIBRARY,
            BuiltInLootTables.UNDERWATER_RUIN_BIG,
            BuiltInLootTables.WOODLAND_MANSION
    );

    public static final Map<ResourceLocation, ResourceLocation> VANILLA_TO_SH = CHESTS
            .stream()
            .collect(Collectors.toMap(Function.identity(),
                    rl -> ScalingHealth.getId("chests_addition/" + rl.getPath().substring(rl.getPath().indexOf("/")+1))
            ));

    @Override
    protected void addTables() {
        blockLootTables.put(Registration.HEART_CRYSTAL_ORE.get(),
                createSilkTouchTable("heart_crystal_ore", Registration.HEART_CRYSTAL_ORE.get(), Registration.HEART_CRYSTAL_SHARD.get()));
        blockLootTables.put(Registration.DEEPLSATE_HEART_CRYSTAL_ORE.get(),
                createSilkTouchTable("deepslate_heart_crystal_ore", Registration.DEEPLSATE_HEART_CRYSTAL_ORE.get(), Registration.HEART_CRYSTAL_SHARD.get()));
        blockLootTables.put(Registration.POWER_CRYSTAL_ORE.get(),
                createSilkTouchTable("power_crystal_ore", Registration.POWER_CRYSTAL_ORE.get(), Registration.POWER_CRYSTAL_SHARD.get()));
        blockLootTables.put(Registration.DEEPSLATE_POWER_CRYSTAL_ORE.get(),
                createSilkTouchTable("deepslate_power_crystal_ore", Registration.DEEPSLATE_POWER_CRYSTAL_ORE.get(), Registration.POWER_CRYSTAL_SHARD.get()));

        LootTable.Builder builder = LootTable.lootTable().withPool(
                new LootPool.Builder()
                        .setRolls(UniformGenerator.between(0, 1))
                        .add(LootItem.lootTableItem(Registration.HEART_CRYSTAL.get())
                                .setWeight(3).setQuality(2)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1,2)))
                        ).add(LootItem.lootTableItem(Registration.CURSED_HEART.get())
                        .setWeight(1).setQuality(5)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))
                ).add(LootItem.lootTableItem(Registration.ENCHANTED_HEART.get())
                        .setWeight(1).setQuality(5)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))
                ).add(LootItem.lootTableItem(Registration.CHANCE_HEART.get())
                        .setWeight(1).setQuality(5)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))
                ).add(LootItem.lootTableItem(Registration.POWER_CRYSTAL.get())
                        .setWeight(2).setQuality(7)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))
                ).add(EmptyLootItem.emptyItem().setWeight(10)));

        VANILLA_TO_SH.values().forEach(rl -> lootTables.put(rl, builder));

        mobLootTable.put(EntityGroup.HOSTILE,
                createSHDropsTable(
                        createSHDropsPool("crystals", 1,
                                new MobLootCondition(true, false, 0, 0.055f, 0.005f),
                                new MobLootEntry(Registration.HEART_CRYSTAL.get(), 12, 1, 0),
                                new MobLootEntry(Registration.POWER_CRYSTAL.get(), 4, 1, 0),
                                new MobLootEntry(Registration.HEART_CRYSTAL_SHARD.get(), 2, 11, 3),
                                new MobLootEntry(Registration.POWER_CRYSTAL_SHARD.get(), 1, 12, 2)),
                        createSHDropsPool("extras", 1,
                                new MobLootCondition(false, false, 10, 0.025f, 0.005f),
                                new MobLootEntry(Registration.BANDAGES.get(), 10, 2, 0),
                                new MobLootEntry(Registration.MEDKIT.get(), 1, 1, 0)),
                        createSHDropsPool("difficulty_mutators", 1,
                                new MobLootCondition(false, false, 70, 0.015f, 0.025f),
                                new MobLootEntry(Registration.CHANCE_HEART.get(), 1, 1, 0),
                                new MobLootEntry(Registration.ENCHANTED_HEART.get(), 1, 1, 0)),
                        createSHDropsPool("blights", 1,
                                new MobLootCondition(true, true, 0, 0, 0),
                                new MobLootEntry(Registration.HEART_CRYSTAL.get(), 10, 3, 1),
                                new MobLootEntry(Registration.POWER_CRYSTAL.get(), 5, 2, 1))
                ));
//        mobLootTable.put(EntityGroup.BOSS,
//                createSHDropsTable(
//                        createSHDropsPool("crystals", 3,
//                                new MobLootCondition(true, false, 0, 0.055f, 0.005f),
//                                new MobLootEntry(Registration.HEART_CRYSTAL.get(), 12, 4, 2),
//                                new MobLootEntry(Registration.POWER_CRYSTAL.get(), 6, 2, 1),
//                                new MobLootEntry(Registration.HEART_CRYSTAL_SHARD.get(), 2, 40, 12),
//                                new MobLootEntry(Registration.POWER_CRYSTAL_SHARD.get(), 1, 32, 10)),
//                        createSHDropsPool("difficulty_mutators", 1,
//                                new MobLootCondition(false, false, 125, 0.5f, 0.01f),
//                                new MobLootEntry(Registration.CURSED_HEART.get(), 1, 2, 0),
//                                new MobLootEntry(Registration.CHANCE_HEART.get(), 2, 2, 0),
//                                new MobLootEntry(Registration.ENCHANTED_HEART.get(), 1, 2, 0)),
//                        createSHDropsPool("blights", 1,
//                                new MobLootCondition(true, true, 0, 0, 0),
//                                new MobLootEntry(Registration.HEART_CRYSTAL.get(), 10, 3, 1),
//                                new MobLootEntry(Registration.POWER_CRYSTAL.get(), 5, 2, 1))
//                ));
        mobLootTable.put(EntityGroup.PEACEFUL,
                createSHDropsTable(
                        createSHDropsPool("crystals", 1,
                                new MobLootCondition(true, false, 0, 0.055f, 0.005f),
                                new MobLootEntry(Registration.HEART_CRYSTAL_SHARD.get(), 2, 8, 1),
                                new MobLootEntry(Registration.POWER_CRYSTAL_SHARD.get(), 1, 5, 0)),
                        createSHDropsPool("difficulty_mutators", 1,
                                new MobLootCondition(false, false, 0, 0.015f, 0.025f),
                                new MobLootEntry(Registration.CURSED_HEART.get(), 1, 1, 0)),
                        createSHDropsPool("blights", 1,
                                new MobLootCondition(true, true, 0, 0, 0),
                                new MobLootEntry(Registration.HEART_CRYSTAL.get(), 5, 1, -1),
                                new MobLootEntry(Registration.POWER_CRYSTAL.get(), 2, 1, -1))
                ));
    }
}