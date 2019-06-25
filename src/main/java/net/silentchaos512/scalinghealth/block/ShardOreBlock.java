package net.silentchaos512.scalinghealth.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;

public class ShardOreBlock extends OreBlock {
    public ShardOreBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(3, 15));
    }

    @Override
    public int getExpDrop(BlockState state, IWorldReader reader, BlockPos pos, int fortune, int silkTouch) {
        return silkTouch == 0 ? MathHelper.nextInt(RANDOM, 1, 5) : 0;
    }
}
