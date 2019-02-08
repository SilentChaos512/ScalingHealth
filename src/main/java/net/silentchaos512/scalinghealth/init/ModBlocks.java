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
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.block.BlockShardOre;

public final class ModBlocks {
    public static BlockShardOre crystalOre;

    private ModBlocks() {}

    public static void registerAll(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> reg = event.getRegistry();
        crystalOre = register(reg, "heart_shard_ore", new BlockShardOre(() -> ModItems.crystalShard));
    }

    private static <T extends Block> T register(IForgeRegistry<Block> reg, String name, T block) {
        ResourceLocation registryName = new ResourceLocation(ScalingHealth.MOD_ID, name);
        block.setRegistryName(registryName);
        reg.register(block);

        ItemBlock item = new ItemBlock(block, new Item.Builder());
        ModItems.blocksToRegister.add(item);

        return block;
    }
}
