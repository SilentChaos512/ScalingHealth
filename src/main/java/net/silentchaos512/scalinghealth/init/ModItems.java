/*
 * Scaling Health
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

import net.minecraft.item.Item;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.item.ItemDifficultyChanger;
import net.silentchaos512.scalinghealth.item.ItemHealing;
import net.silentchaos512.scalinghealth.item.ItemHeartContainer;

public class ModItems {
    public static final ItemHeartContainer heart = new ItemHeartContainer();
    public static final Item crystalShard = new Item();
    public static final Item heartDust = new Item();
    public static final ItemHealing healingItem = new ItemHealing();
    public static final ItemDifficultyChanger difficultyChanger = new ItemDifficultyChanger();

    public static void registerAll(SRegistry reg) {
        reg.registerItem(heart, "heartcontainer");
        reg.registerItem(crystalShard, "crystalshard");
        reg.registerItem(heartDust, "heartdust");
        reg.registerItem(healingItem, "healingitem");
        reg.registerItem(difficultyChanger, "difficultychanger");
    }
}
