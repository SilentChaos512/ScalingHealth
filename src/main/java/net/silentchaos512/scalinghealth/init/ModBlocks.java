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

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.silentchaos512.lib.block.BlockOreSL;
import net.silentchaos512.lib.registry.IRegistrationHandler;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.config.Config;

import java.util.Random;

public class ModBlocks implements IRegistrationHandler<Block> {

    public static BlockOreSL crystalOre = new BlockOreSL(ModItems.crystalShard, 2, 1, 1, 1, 5) {
        @Override
        public int quantityDropped(Random random) {
            return Config.HEART_CRYSTAL_ORE_QUANTITY_DROPPED;
        }

        @Override
        public float bonusAmount(int fortune, Random random) {
            return (fortune - 1) * random.nextFloat() - 1f;
        }
    };

    @Override
    public void registerAll(SRegistry reg) {
        reg.registerBlock(crystalOre, "crystalore", new ItemBlock(crystalOre));
    }
}
