/*
 * Scaling Health -- ModLoot
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.scalinghealth.init;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.silentchaos512.scalinghealth.loot.conditions.SHMobProperties;

import java.util.List;

public final class ModLoot {
    private ModLoot() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(ModLoot::onLootTableLoad);

        LootConditionManager.registerCondition(SHMobProperties.SERIALIZER);
    }

    private static final List<ResourceLocation> ADD_ITEMS_TO = ImmutableList.of(
            LootTables.CHESTS_ABANDONED_MINESHAFT,
            LootTables.CHESTS_BURIED_TREASURE,
            LootTables.CHESTS_NETHER_BRIDGE,
            LootTables.CHESTS_SIMPLE_DUNGEON,
            LootTables.CHESTS_STRONGHOLD_LIBRARY,
            LootTables.CHESTS_UNDERWATER_RUIN_BIG,
            LootTables.CHESTS_WOODLAND_MANSION
    );

    private static void onLootTableLoad(LootTableLoadEvent event) {
        if (ADD_ITEMS_TO.contains(event.getName())) {
            event.getTable().addPool((new LootPool.Builder())
                    .name("scalinghealth_added")
                    .rolls(new RandomValueRange(1))
                    .addEntry(ItemLootEntry.func_216168_a(ModItems.HEART_CRYSTAL)
                            .weight(3)
                            .quality(2)
                            .acceptFunction(SetCount.func_215932_a(new RandomValueRange(1, 2)))
                    )
                    .addEntry(ItemLootEntry.func_216168_a(ModItems.POWER_CRYSTAL)
                            .weight(2)
                            .quality(7)
                            .acceptFunction(SetCount.func_215932_a(new RandomValueRange(1, 2)))
                    )
                    .addEntry(ItemLootEntry.func_216168_a(ModItems.CURSED_HEART)
                            .weight(1)
                            .quality(5)
                            .acceptFunction(SetCount.func_215932_a(new RandomValueRange(1, 3)))
                    )
                    .addEntry(ItemLootEntry.func_216168_a(ModItems.ENCHANTED_HEART)
                            .weight(1)
                            .quality(5)
                            .acceptFunction(SetCount.func_215932_a(new RandomValueRange(1, 3)))
                    )
                    .addEntry(EmptyLootEntry.func_216167_a().weight(10))
                    .build());
        }
    }
}
