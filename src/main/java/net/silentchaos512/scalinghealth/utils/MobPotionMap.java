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

import net.minecraft.potion.Potion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobPotionMap {
    List<PotionEntry> list = new ArrayList<>();
    List<PotionEntry> temp = new ArrayList<>();

    public void put(Potion potion, int weight, int amplifier) {
        list.add(new PotionEntry(potion, weight, amplifier));
    }

    public void clear() {
        list.clear();
    }

    public PotionEntry getRandom(Random rand, int maxWeight) {
        temp.clear();
        for (PotionEntry pot : list) {
            if (pot.cost <= maxWeight) {
                temp.add(pot);
            }
        }

        if (temp.isEmpty()) {
            return null;
        }
        return temp.get(rand.nextInt(temp.size()));
    }

    public static class PotionEntry {
        public final Potion potion;
        public final int cost;
        public final int amplifier;

        public PotionEntry(Potion potion, int cost, int amplifier) {
            this.potion = potion;
            this.cost = cost;
            this.amplifier = amplifier;
        }
    }
}
