package net.silentchaos512.scalinghealth.objects.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.OreBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ShardOreBlock extends OreBlock {
    public ShardOreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public int getExpDrop(BlockState state, LevelReader reader, BlockPos pos, int fortune, int silkTouch) {
        return silkTouch == 0 ? Mth.nextInt(RANDOM, 1, 5) : 0;
    }
}
