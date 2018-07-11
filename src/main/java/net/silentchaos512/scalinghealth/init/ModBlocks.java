/*
 * ModBlocks
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.scalinghealth.init;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.silentchaos512.lib.registry.IRegistrationHandler;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.block.BlockCrystalOre;

public class ModBlocks implements IRegistrationHandler<Block> {

    public static BlockCrystalOre crystalOre = new BlockCrystalOre();

    @Override
    public void registerAll(SRegistry reg) {
        reg.registerBlock(crystalOre, "crystalore", new ItemBlock(crystalOre));
    }
}
