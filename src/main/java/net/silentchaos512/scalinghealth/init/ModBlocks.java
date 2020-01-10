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
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.lib.block.IBlockProvider;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.block.ShardOreBlock;
import net.silentchaos512.utils.Lazy;

import java.util.Locale;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum ModBlocks implements IBlockProvider {
    HEART_CRYSTAL_ORE(ShardOreBlock::new),
    POWER_CRYSTAL_ORE(ShardOreBlock::new);

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

    @SubscribeEvent
    public static void registerAll(RegistryEvent.Register<Block> event) {
        // Workaround for Forge event bus bug TODO
        if (!event.getName().equals(ForgeRegistries.BLOCKS.getRegistryName())) return;

        for (ModBlocks block : values()) {
            ScalingHealth.LOGGER.debug("Hey registed block");
            register(block.getName(), block.asBlock());
        }
    }

    private static void register(String name, Block block) {
        ResourceLocation registryName = new ResourceLocation(ScalingHealth.MOD_ID, name);
        block.setRegistryName(registryName);
        ForgeRegistries.BLOCKS.register(block);

        BlockItem item = new BlockItem(block, new Item.Properties().group(ScalingHealth.SH));
        item.setRegistryName(registryName);
        ModItems.blocksToRegister.add(item);
    }
}
