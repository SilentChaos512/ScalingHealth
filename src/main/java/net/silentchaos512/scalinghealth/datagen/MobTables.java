package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.loot.conditions.SHMobProperties;
import net.silentchaos512.scalinghealth.objects.Registration;
import net.silentchaos512.scalinghealth.utils.EntityGroup;

import java.util.Locale;
import java.util.function.BiConsumer;

public class MobTables implements LootTableSubProvider {
    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
        consumer.accept(fromGroup(EntityGroup.HOSTILE),
                createSHDropsTable(
                        createSHDropsPool( 1,
                                new MobLootCondition(true, false, 0, 0.055f, 0.005f),
                                new MobLootEntry(Registration.HEART_CRYSTAL.get(), 12, 1, 0),
                                new MobLootEntry(Registration.POWER_CRYSTAL.get(), 4, 1, 0),
                                new MobLootEntry(Registration.HEART_CRYSTAL_SHARD.get(), 2, 11, 3),
                                new MobLootEntry(Registration.POWER_CRYSTAL_SHARD.get(), 1, 12, 2)),
                        createSHDropsPool( 1,
                                new MobLootCondition(false, false, 10, 0.025f, 0.005f),
                                new MobLootEntry(Registration.BANDAGES.get(), 10, 2, 0),
                                new MobLootEntry(Registration.MEDKIT.get(), 1, 1, 0)),
                        createSHDropsPool(1,
                                new MobLootCondition(false, false, 70, 0.015f, 0.025f),
                                new MobLootEntry(Registration.CHANCE_HEART.get(), 1, 1, 0),
                                new MobLootEntry(Registration.ENCHANTED_HEART.get(), 1, 1, 0)),
                        createSHDropsPool( 1,
                                new MobLootCondition(true, true, 0, 0, 0),
                                new MobLootEntry(Registration.HEART_CRYSTAL.get(), 10, 3, 1),
                                new MobLootEntry(Registration.POWER_CRYSTAL.get(), 5, 2, 1))
                ));
        consumer.accept(fromGroup(EntityGroup.PEACEFUL),
                createSHDropsTable(
                        createSHDropsPool(1,
                                new MobLootCondition(true, false, 0, 0.055f, 0.005f),
                                new MobLootEntry(Registration.HEART_CRYSTAL_SHARD.get(), 2, 8, 1),
                                new MobLootEntry(Registration.POWER_CRYSTAL_SHARD.get(), 1, 5, 0)),
                        createSHDropsPool(1,
                                new MobLootCondition(false, false, 0, 0.015f, 0.025f),
                                new MobLootEntry(Registration.CURSED_HEART.get(), 1, 1, 0)),
                        createSHDropsPool(1,
                                new MobLootCondition(true, true, 0, 0, 0),
                                new MobLootEntry(Registration.HEART_CRYSTAL.get(), 5, 1, -1),
                                new MobLootEntry(Registration.POWER_CRYSTAL.get(), 2, 1, -1))
                ));
    }

    public static ResourceLocation fromGroup(EntityGroup group) {
        return ScalingHealth.getId("bonus_drops/" + group.name().toLowerCase(Locale.ROOT));
    }

    public static LootTable.Builder createSHDropsTable(LootPool.Builder... pools) {
        LootTable.Builder table = LootTable.lootTable();
        for (LootPool.Builder pool : pools)
            table.withPool(pool);
        return table;
    }

    public static LootPool.Builder createSHDropsPool(int rolls, MobLootCondition conditions, MobLootEntry... entries) {
        LootPool.Builder builder = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(rolls));

        for (MobLootEntry entry : entries) {
            if (entry.isCount())
                builder.add(
                        LootItem.lootTableItem(entry.item).
                                setWeight(entry.weight).
                                apply(SetItemCountFunction.setCount(ConstantValue.exactly(entry.max))));
            else
                builder.add(
                        LootItem.lootTableItem(entry.item).
                                setWeight(entry.weight).
                                apply(SetItemCountFunction.setCount(UniformGenerator.between(entry.min, entry.max))));
        }

        if (conditions.hasDiffCond() || conditions.blight)
            builder.when(SHMobProperties.builder(LootContext.EntityTarget.THIS, conditions.blight, conditions.difficulty, Integer.MAX_VALUE));
        if (conditions.hasLootCond())
            builder.when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(conditions.lootingChance, conditions.lootingMulti));
        if (conditions.playerKill)
            builder.when(LootItemKilledByPlayerCondition.killedByPlayer());

        return builder;
    }

    public static class MobLootEntry {
        int weight;
        int max;
        int min;

        Item item;

        MobLootEntry(Item item, int weight, int max, int min) {
            this.max = max;
            this.min = min;
            this.weight = weight;
            this.item = item;
        }

        //If false, a max and min should be used. if not a simple count.
        boolean isCount() {
            return min == 0 && max == 1;
        }
    }

    public static class MobLootCondition {
        boolean playerKill;
        boolean blight;
        int difficulty;
        float lootingChance;
        float lootingMulti;

        MobLootCondition(boolean pk, boolean blight, int difficulty, float lootingChance, float lootingMulti) {
            this.difficulty = difficulty;
            this.lootingChance = lootingChance;
            this.lootingMulti = lootingMulti;
            this.playerKill = pk;
            this.blight = blight;
        }

        boolean hasDiffCond() {
            return difficulty != 0;
        }

        boolean hasLootCond() {
            return lootingMulti != 0 && lootingChance != 0;
        }
    }
}
