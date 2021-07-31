package net.silentchaos512.scalinghealth.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.loot.conditions.SHMobProperties;
import net.silentchaos512.scalinghealth.utils.EntityGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class BaseLootTableGenerator extends LootTableProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    protected final Map<ResourceLocation, LootTable.Builder> lootTables = new HashMap<>();
    protected final Map<Block, LootTable.Builder> blockLootTables = new HashMap<>();
    protected final Map<EntityGroup, LootTable.Builder> mobLootTable = new HashMap<>();
    private final DataGenerator generator;

    public BaseLootTableGenerator(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
        this.generator = dataGeneratorIn;
    }

    protected abstract void addTables();

    protected LootTable.Builder createSilkTouchTable(String name, Block block, Item lootItem) {
        LootPool.Builder builder = LootPool.lootPool()
                .name(name)
                .setRolls(ConstantValue.exactly(1))
                .add(AlternativesEntry.alternatives(
                        LootItem.lootTableItem(block)
                                .when(MatchTool.toolMatches(ItemPredicate.Builder.item()
                                        .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))))),
                        LootItem.lootTableItem(lootItem)
                                .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
                                .apply(ApplyExplosionDecay.explosionDecay())
                        ).when(ExplosionCondition.survivesExplosion())
                );
        return LootTable.lootTable().withPool(builder);
    }

    protected LootTable.Builder createSHDropsTable(LootPool.Builder... pools){
        LootTable.Builder table = LootTable.lootTable();
        for(LootPool.Builder pool : pools)
            table.withPool(pool);
        return table;
    }

    protected LootPool.Builder createSHDropsPool(String name, int rolls, MobLootCondition conditions, MobLootEntry... entries) {
        LootPool.Builder builder = LootPool.lootPool()
                .name(name)
                .setRolls(ConstantValue.exactly(rolls));

        for(MobLootEntry entry : entries){
            if(entry.isCount())
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

        if(conditions.hasDiffCond() || conditions.blight)
            builder.when(SHMobProperties.builder(LootContext.EntityTarget.THIS, conditions.blight, conditions.difficulty, Integer.MAX_VALUE));
        if(conditions.hasLootCond())
            builder.when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(conditions.lootingChance, conditions.lootingMulti));
        if(conditions.playerKill)
            builder.when(LootItemKilledByPlayerCondition.killedByPlayer());

        return builder;
    }

    @Override
    public void run(HashCache cache) {
        addTables();

        Map<ResourceLocation, LootTable> tables = new HashMap<>();
        for (Map.Entry<Block, LootTable.Builder> entry : blockLootTables.entrySet()) {
            tables.put(entry.getKey().getLootTable(), entry.getValue().setParamSet(LootContextParamSets.BLOCK).build());
        }
        for(Map.Entry<EntityGroup, LootTable.Builder> entry : mobLootTable.entrySet()) {
            tables.put(ScalingHealth.getId("bonus_drops/" + entry.getKey().name().toLowerCase(Locale.ROOT)), entry.getValue().build());
        }
        lootTables.forEach((rl, b) -> tables.put(rl, b.build()));
        writeTables(cache, tables);
    }

    private void writeTables(HashCache cache, Map<ResourceLocation, LootTable> tables) {
        Path outputFolder = this.generator.getOutputFolder();
        tables.forEach((key, lootTable) -> {
            Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json");
            try {
                DataProvider.save(GSON, cache, LootTables.serialize(lootTable), path);
            } catch (IOException e) {
                LOGGER.error("Couldn't write loot table {}", path, e);
            }
        });
    }

    @Override
    public String getName() {
        return "Scaling Health LootTables";
    }

    protected static class MobLootEntry{
        int weight;
        int max;
        int min;

        Item item;

        MobLootEntry(Item item, int weight, int max, int min){
            this.max = max;
            this.min = min;
            this.weight = weight;
            this.item = item;
        }

        //If false, a max and min should be used. if not a simple count.
        boolean isCount(){
            return min == 0 && max == 1;
        }
    }

    protected static class MobLootCondition {
        boolean playerKill;
        boolean blight;
        int difficulty;
        float lootingChance;
        float lootingMulti;

        MobLootCondition(boolean pk, boolean blight, int difficulty, float lootingChance, float lootingMulti){
            this.difficulty = difficulty;
            this.lootingChance = lootingChance;
            this.lootingMulti = lootingMulti;
            this.playerKill = pk;
            this.blight = blight;
        }

        boolean hasDiffCond(){
            return difficulty != 0;
        }

        boolean hasLootCond(){
            return lootingMulti != 0 && lootingChance != 0;
        }
    }
}
