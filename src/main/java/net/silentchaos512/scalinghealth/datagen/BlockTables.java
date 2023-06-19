package net.silentchaos512.scalinghealth.datagen;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.silentchaos512.scalinghealth.objects.Registration;

import java.util.Collections;
import java.util.Set;

public class BlockTables extends BlockLootSubProvider {
    //keep a list of SH blocks otherwise default generation is the whole block registry
    private final Set<Block> knownBlocks = new ReferenceOpenHashSet<>();

    protected BlockTables() {
        super(Collections.emptySet(), FeatureFlags.VANILLA_SET);
    }

    @Override
    protected void add(Block block, LootTable.Builder builder) {
        super.add(block, builder);
        knownBlocks.add(block);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return knownBlocks;
    }

    @Override
    protected void generate() {
        add(Registration.HEART_CRYSTAL_ORE.get(), createSilkTouchTable(Registration.HEART_CRYSTAL_ORE.get(), Registration.HEART_CRYSTAL_SHARD.get()));
        add(Registration.DEEPLSATE_HEART_CRYSTAL_ORE.get(), createSilkTouchTable(Registration.DEEPLSATE_HEART_CRYSTAL_ORE.get(), Registration.HEART_CRYSTAL_SHARD.get()));
        add(Registration.POWER_CRYSTAL_ORE.get(), createSilkTouchTable(Registration.POWER_CRYSTAL_ORE.get(), Registration.POWER_CRYSTAL_SHARD.get()));
        add(Registration.DEEPSLATE_POWER_CRYSTAL_ORE.get(), createSilkTouchTable(Registration.DEEPSLATE_POWER_CRYSTAL_ORE.get(), Registration.POWER_CRYSTAL_SHARD.get()));
    }

    public static LootTable.Builder createSilkTouchTable(Block block, Item lootItem) {
        LootPool.Builder builder = LootPool.lootPool()
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
}
