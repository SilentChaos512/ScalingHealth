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
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.properties.EntityPropertyManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.silentchaos512.scalinghealth.loot.properties.PropertyBlight;
import net.silentchaos512.scalinghealth.loot.properties.PropertyDifficulty;

import java.util.List;

public final class ModLoot {
    private ModLoot() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(ModLoot::onLootTableLoad);

        EntityPropertyManager.registerProperty(new PropertyBlight.Serializer());
        EntityPropertyManager.registerProperty(new PropertyDifficulty.Serializer());
    }

    private static final List<ResourceLocation> ADD_ITEMS_TO = ImmutableList.of(
            LootTableList.CHESTS_ABANDONED_MINESHAFT,
            LootTableList.CHESTS_BURIED_TREASURE,
            LootTableList.CHESTS_NETHER_BRIDGE,
            LootTableList.CHESTS_SIMPLE_DUNGEON,
            LootTableList.CHESTS_STRONGHOLD_LIBRARY,
            LootTableList.CHESTS_UNDERWATER_RUIN_BIG,
            LootTableList.CHESTS_WOODLAND_MANSION
    );

    private static void onLootTableLoad(LootTableLoadEvent event) {
        if (ADD_ITEMS_TO.contains(event.getName())) {
            LootPool main = event.getTable().getPool("main");
            //noinspection ConstantConditions -- pool can be null
            if (main != null) {
                main.addEntry(new LootEntryItem(ModItems.heart, 3, 2, new LootFunction[]{
                        new SetCount(new LootCondition[0], new RandomValueRange(1, 2))
                }, new LootCondition[0], "sh_heart_container"));
                main.addEntry(new LootEntryItem(ModItems.cursedHeart, 1, 5, new LootFunction[]{
                        new SetCount(new LootCondition[0], new RandomValueRange(1, 3))
                }, new LootCondition[0], "sh_cursed_heart"));
                main.addEntry(new LootEntryItem(ModItems.enchantedHeart, 1, 5, new LootFunction[]{
                        new SetCount(new LootCondition[0], new RandomValueRange(1, 3))
                }, new LootCondition[0], "sh_enchanted_heart"));
            }
        }
    }
}
