package net.silentchaos512.scalinghealth.datagen;

import com.google.common.collect.Lists;
import net.minecraft.data.DataGenerator;
import net.minecraft.loot.*;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.util.ResourceLocation;
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
            LootTables.CHESTS_ABANDONED_MINESHAFT,
            LootTables.CHESTS_BURIED_TREASURE,
            LootTables.CHESTS_NETHER_BRIDGE,
            LootTables.CHESTS_SIMPLE_DUNGEON,
            LootTables.CHESTS_STRONGHOLD_LIBRARY,
            LootTables.CHESTS_UNDERWATER_RUIN_BIG,
            LootTables.CHESTS_WOODLAND_MANSION
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
        blockLootTables.put(Registration.POWER_CRYSTAL_ORE.get(),
                createSilkTouchTable("power_crystal_ore", Registration.POWER_CRYSTAL_ORE.get(), Registration.POWER_CRYSTAL_SHARD.get()));

        LootTable.Builder builder = LootTable.builder().addLootPool(
                new LootPool.Builder()
                        .rolls(new RandomValueRange(1))
                        .addEntry(ItemLootEntry.builder(Registration.HEART_CRYSTAL.get())
                                .weight(3).quality(2)
                                .acceptFunction(SetCount.builder(new RandomValueRange(1,2)))
                        ).addEntry(ItemLootEntry.builder(Registration.CURSED_HEART.get())
                        .weight(1).quality(5)
                        .acceptFunction(SetCount.builder(new RandomValueRange(1, 3)))
                ).addEntry(ItemLootEntry.builder(Registration.ENCHANTED_HEART.get())
                        .weight(1).quality(5)
                        .acceptFunction(SetCount.builder(new RandomValueRange(1, 3)))
                ).addEntry(ItemLootEntry.builder(Registration.CHANCE_HEART.get())
                        .weight(1).quality(5)
                        .acceptFunction(SetCount.builder(new RandomValueRange(1, 3)))
                ).addEntry(ItemLootEntry.builder(Registration.POWER_CRYSTAL.get())
                        .weight(2).quality(7)
                        .acceptFunction(SetCount.builder(new RandomValueRange(1, 2)))
                ).addEntry(EmptyLootEntry.func_216167_a().weight(10)));

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