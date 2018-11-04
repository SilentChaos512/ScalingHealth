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

package net.silentchaos512.scalinghealth.utils;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.silentchaos512.scalinghealth.ScalingHealth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EquipmentTierMap {
    public final int tierCount;
    public final EntityEquipmentSlot slot;

    List<List<StackProducer>> sets;

    public EquipmentTierMap(int tierCount, EntityEquipmentSlot slot) {
        this.tierCount = tierCount;
        this.slot = slot;

        sets = new ArrayList<>();
        for (int i = 0; i < tierCount; ++i) {
            sets.add(new ArrayList<>());
        }
    }

    public void put(ItemStack stack, int tier) {
        put(new StackProducer(stack), tier);
    }

    public void put(StackProducer producer, int tier) {
        if (tier < 0 || tier >= tierCount) {
            throw new IllegalArgumentException("tier must be between 0 and " + tierCount);
        }

        // TODO: We could also check the stack is valid for the slot.

        sets.get(tier).add(producer);
    }

    public ItemStack getRandom(int tier) {
        if (tier < 0 || tier >= tierCount) {
            throw new IllegalArgumentException("tier must be between 0 and " + tierCount);
        }

        List<StackProducer> list = sets.get(tier);
        if (list.isEmpty()) {
            return ItemStack.EMPTY;
        }

        Random rand = ScalingHealth.random;
        return list.get(rand.nextInt(list.size())).get(rand);
    }

    public ItemStack get(int tier, int index) {
        if (tier < 0 || tier >= tierCount) {
            throw new IllegalArgumentException("tier must be between 0 and " + tierCount);
        }

        List<StackProducer> list = sets.get(tier);
        if (list.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (index < 0 || index >= list.size()) {
            throw new IllegalArgumentException("index must be between 0 and " + list.size());
        }

        return list.get(index).get(ScalingHealth.random);
    }
}
