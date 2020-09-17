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
import net.minecraft.util.ResourceLocation;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.KilledByPlayer;
import net.minecraft.loot.conditions.MatchTool;
import net.minecraft.loot.conditions.RandomChanceWithLooting;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.loot.functions.ApplyBonus;
import net.minecraft.loot.functions.ExplosionDecay;
import net.minecraft.loot.functions.SetCount;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.lib.EntityGroup;
import net.silentchaos512.scalinghealth.loot.conditions.SHMobProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class BaseLootTable extends LootTableProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    protected final Map<Block, LootTable.Builder> lootTables = new HashMap<>();
    protected final Map<EntityGroup, LootTable.Builder> mobLootTable = new HashMap<>();
    private final DataGenerator generator;

    public BaseLootTable(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
        this.generator = dataGeneratorIn;
    }

    protected abstract void addTables();

    protected LootTable.Builder createSilkTouchTable(String name, Block block, Item lootItem) {
        LootPool.Builder builder = LootPool.builder()
                .name(name)
                .rolls(ConstantRange.of(1))
                .addEntry(AlternativesLootEntry.builder(
                        ItemLootEntry.builder(block)
                                .acceptCondition(MatchTool.builder(ItemPredicate.Builder.create()
                                        .enchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.IntBound.atLeast(1))))),
                        ItemLootEntry.builder(lootItem)
                                .acceptFunction(ApplyBonus.oreDrops(Enchantments.FORTUNE))
                                .acceptFunction(ExplosionDecay.builder())
                        ).acceptCondition(SurvivesExplosion.builder())
                );
        return LootTable.builder().addLootPool(builder);
    }

    protected LootTable.Builder createSHDropsTable(LootPool.Builder... pools){
        LootTable.Builder table = LootTable.builder();
        for(LootPool.Builder pool : pools)
            table.addLootPool(pool);
        return table;
    }

    protected LootPool.Builder createSHDropsPool(String name, int rolls, MobLootCondition conditions, MobLootEntry... entries) {
        LootPool.Builder builder = LootPool.builder()
                .name(name)
                .rolls(ConstantRange.of(rolls));

        for(MobLootEntry entry : entries){
            if(entry.isCount())
                builder.addEntry(
                        ItemLootEntry.builder(entry.item).
                                weight(entry.weight).
                                acceptFunction(SetCount.builder(ConstantRange.of(entry.max))));
            else
                builder.addEntry(
                        ItemLootEntry.builder(entry.item).
                                weight(entry.weight).
                                acceptFunction(SetCount.builder(RandomValueRange.of(entry.min, entry.max))));
        }

        if(conditions.hasDiffCond() || conditions.blight)
            builder.acceptCondition(SHMobProperties.builder(LootContext.EntityTarget.THIS, conditions.blight, conditions.difficulty, Integer.MAX_VALUE));
        if(conditions.hasLootCond())
            builder.acceptCondition(RandomChanceWithLooting.builder(conditions.lootingChance, conditions.lootingMulti));
        if(conditions.playerKill)
            builder.acceptCondition(KilledByPlayer.builder());

        return builder;
    }

    @Override
    public void act(DirectoryCache cache) {
        addTables();

        Map<ResourceLocation, LootTable> tables = new HashMap<>();
        for (Map.Entry<Block, LootTable.Builder> entry : lootTables.entrySet()) {
            tables.put(entry.getKey().getLootTable(), entry.getValue().setParameterSet(LootParameterSets.BLOCK).build());
        }
        for(Map.Entry<EntityGroup, LootTable.Builder> entry : mobLootTable.entrySet()){
            tables.put(ScalingHealth.getId("bonus_drops/" + entry.getKey().name().toLowerCase(Locale.ROOT)), entry.getValue().setParameterSet(LootParameterSets.GENERIC).build());
        }
        writeTables(cache, tables);
    }

    private void writeTables(DirectoryCache cache, Map<ResourceLocation, LootTable> tables) {
        Path outputFolder = this.generator.getOutputFolder();
        tables.forEach((key, lootTable) -> {
            Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json");
            try {
                IDataProvider.save(GSON, cache, LootTableManager.toJson(lootTable), path);
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
