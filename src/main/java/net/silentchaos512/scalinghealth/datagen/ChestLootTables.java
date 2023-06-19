package net.silentchaos512.scalinghealth.datagen;

import com.google.common.collect.Lists;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.data.loot.packs.VanillaChestLoot;
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

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChestLootTables implements LootTableSubProvider {
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
                    rl -> ScalingHealth.getId("chests_addition/" + rl.getPath().substring(rl.getPath().indexOf("/") + 1))
            ));

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
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

        VANILLA_TO_SH.values().forEach(rl -> consumer.accept(rl, builder));
    }
}
