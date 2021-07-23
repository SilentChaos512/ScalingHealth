package net.silentchaos512.scalinghealth.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.LootTableProvider;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.KilledByPlayer;
import net.minecraft.loot.conditions.MatchTool;
import net.minecraft.loot.conditions.RandomChanceWithLooting;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.loot.functions.ApplyBonus;
import net.minecraft.loot.functions.ExplosionDecay;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.util.ResourceLocation;
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
                .setRolls(ConstantRange.exactly(1))
                .add(AlternativesLootEntry.alternatives(
                        ItemLootEntry.lootTableItem(block)
                                .when(MatchTool.toolMatches(ItemPredicate.Builder.item()
                                        .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.IntBound.atLeast(1))))),
                        ItemLootEntry.lootTableItem(lootItem)
                                .apply(ApplyBonus.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
                                .apply(ExplosionDecay.explosionDecay())
                        ).when(SurvivesExplosion.survivesExplosion())
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
                .setRolls(ConstantRange.exactly(rolls));

        for(MobLootEntry entry : entries){
            if(entry.isCount())
                builder.add(
                        ItemLootEntry.lootTableItem(entry.item).
                                setWeight(entry.weight).
                                apply(SetCount.setCount(ConstantRange.exactly(entry.max))));
            else
                builder.add(
                        ItemLootEntry.lootTableItem(entry.item).
                                setWeight(entry.weight).
                                apply(SetCount.setCount(RandomValueRange.between(entry.min, entry.max))));
        }

        if(conditions.hasDiffCond() || conditions.blight)
            builder.when(SHMobProperties.builder(LootContext.EntityTarget.THIS, conditions.blight, conditions.difficulty, Integer.MAX_VALUE));
        if(conditions.hasLootCond())
            builder.when(RandomChanceWithLooting.randomChanceAndLootingBoost(conditions.lootingChance, conditions.lootingMulti));
        if(conditions.playerKill)
            builder.when(KilledByPlayer.killedByPlayer());

        return builder;
    }

    @Override
    public void run(DirectoryCache cache) {
        addTables();

        Map<ResourceLocation, LootTable> tables = new HashMap<>();
        for (Map.Entry<Block, LootTable.Builder> entry : blockLootTables.entrySet()) {
            tables.put(entry.getKey().getLootTable(), entry.getValue().setParamSet(LootParameterSets.BLOCK).build());
        }
        for(Map.Entry<EntityGroup, LootTable.Builder> entry : mobLootTable.entrySet()) {
            tables.put(ScalingHealth.getId("bonus_drops/" + entry.getKey().name().toLowerCase(Locale.ROOT)), entry.getValue().build());
        }
        lootTables.forEach((rl, b) -> tables.put(rl, b.build()));
        writeTables(cache, tables);
    }

    private void writeTables(DirectoryCache cache, Map<ResourceLocation, LootTable> tables) {
        Path outputFolder = this.generator.getOutputFolder();
        tables.forEach((key, lootTable) -> {
            Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json");
            try {
                IDataProvider.save(GSON, cache, LootTableManager.serialize(lootTable), path);
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
