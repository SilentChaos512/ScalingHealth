package net.silentchaos512.scalinghealth.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.Random;
import java.util.function.Supplier;

public class BlockShardOre extends BlockOre {
    private final Supplier<IItemProvider> drop;

    public BlockShardOre(Supplier<IItemProvider> drop) {
        super(Block.Properties.create(Material.ROCK)
                .hardnessAndResistance(3f, 15f));
        this.drop = drop;
    }

    @Override
    public IItemProvider getItemDropped(IBlockState state, World world, BlockPos pos, int fortune) {
        return this.drop.get();
    }

    @Override
    public int quantityDropped(IBlockState state, Random random) {
        return 1; // TODO: config
    }

    @Override
    public int getItemsToDropCount(IBlockState state, int fortune, World worldIn, BlockPos pos, Random random) {
        if (fortune > 0 && this != this.getItemDropped(this.getStateContainer().getValidStates().iterator().next(), worldIn, pos, fortune)) {
            int quantity = quantityDropped(state, random);
            float bonus = (fortune - 1) * random.nextFloat() - 1f;
            if (bonus < 0) bonus = 0;
            return MathHelper.clamp(Math.round(quantity * (bonus + 1)), 0, 64);
        }
        return quantityDropped(state, random);
    }

    @Override
    public int getExpDrop(IBlockState state, IWorldReader reader, BlockPos pos, int fortune) {
        return MathHelper.nextInt(RANDOM, 1, 5);
    }
}
