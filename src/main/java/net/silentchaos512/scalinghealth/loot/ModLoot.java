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

package net.silentchaos512.scalinghealth.loot;

import com.google.common.collect.ImmutableList;
import net.minecraft.loot.*;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.utils.config.EnabledFeatures;

import java.util.List;

import static net.silentchaos512.scalinghealth.objects.Registration.*;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public final class ModLoot {
    private static final List<ResourceLocation> ADD_ITEMS_TO = ImmutableList.of(
            LootTables.CHESTS_ABANDONED_MINESHAFT,
            LootTables.CHESTS_BURIED_TREASURE,
            LootTables.CHESTS_NETHER_BRIDGE,
            LootTables.CHESTS_SIMPLE_DUNGEON,
            LootTables.CHESTS_STRONGHOLD_LIBRARY,
            LootTables.CHESTS_UNDERWATER_RUIN_BIG,
            LootTables.CHESTS_WOODLAND_MANSION
    );

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (ADD_ITEMS_TO.contains(event.getName())) {
            if(!EnabledFeatures.healthCrystalEnabled() && !EnabledFeatures.difficultyEnabled() && !EnabledFeatures.powerCrystalEnabled())
                return; //have nothing to add.

            event.getTable().addPool(
                    correctEntries(new LootPool.Builder()
                            .name("scalinghealth_added")
                            .rolls(new RandomValueRange(1))
                    )
                    .addEntry(EmptyLootEntry.func_216167_a().weight(10))
                    .build());
        }
    }

    private static LootPool.Builder correctEntries(LootPool.Builder builder){
        if (EnabledFeatures.healthCrystalEnabled())
            builder.addEntry(
                    ItemLootEntry.builder(HEART_CRYSTAL.get())
                            .weight(3).quality(2)
                            .acceptFunction(SetCount.builder(new RandomValueRange(1, 2)))
            );

        if(EnabledFeatures.difficultyEnabled())
            builder.addEntry(
                    ItemLootEntry.builder(CURSED_HEART.get())
                            .weight(1)
                            .quality(5)
                            .acceptFunction(SetCount.builder(new RandomValueRange(1, 3)))
                    )
                    .addEntry(
                            ItemLootEntry.builder(ENCHANTED_HEART.get())
                                    .weight(1)
                                    .quality(5)
                                    .acceptFunction(SetCount.builder(new RandomValueRange(1, 3)))
                    )
                    .addEntry(
                            ItemLootEntry.builder(CHANCE_HEART.get())
                                    .weight(1)
                                    .quality(5)
                                    .acceptFunction(SetCount.builder(new RandomValueRange(1, 3)))
                    );

        if(EnabledFeatures.powerCrystalEnabled())
            builder.addEntry(
                    ItemLootEntry.builder(POWER_CRYSTAL.get())
                            .weight(2)
                            .quality(7)
                            .acceptFunction(SetCount.builder(new RandomValueRange(1, 2)))
            );
        return builder;
    }
}
