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
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.lib.block.IBlockProvider;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.block.BlockShardOre;
import net.silentchaos512.utils.Lazy;

import java.util.Locale;
import java.util.function.Supplier;

public enum ModBlocks implements IBlockProvider {
    HEART_CRYSTAL_ORE(() -> new BlockShardOre(() -> ModItems.HEART_CRYSTAL_SHARD)),
    POWER_CRYSTAL_ORE(() -> new BlockShardOre(() -> ModItems.POWER_CRYSTAL_SHARD));

    private final Lazy<Block> block;

    ModBlocks(Supplier<Block> factory) {
        this.block = Lazy.of(factory);
    }

    @Override
    public Block asBlock() {
        return block.get();
    }

    @Override
    public Item asItem() {
        return asBlock().asItem();
    }

    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static void registerAll(RegistryEvent.Register<Block> event) {
        // Workaround for Forge event bus bug
        if (!event.getName().equals(ForgeRegistries.BLOCKS.getRegistryName())) return;

        for (ModBlocks block : values()) {
            register(block.getName(), block.asBlock());
        }
    }

    private static void register(String name, Block block) {
        ResourceLocation registryName = new ResourceLocation(ScalingHealth.MOD_ID, name);
        block.setRegistryName(registryName);
        ForgeRegistries.BLOCKS.register(block);

        ItemBlock item = new ItemBlock(block, new Item.Properties());
        item.setRegistryName(registryName);
        ModItems.blocksToRegister.add(item);
    }
}
