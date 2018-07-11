package net.silentchaos512.scalinghealth.block;

import java.util.Random;

import net.minecraft.block.BlockOre;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.silentchaos512.lib.block.BlockSL;
import net.silentchaos512.lib.registry.RecipeMaker;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.init.ModItems;

public class BlockCrystalOre extends BlockOre {

    public BlockCrystalOre() {
        setHardness(3.0f);
        setResistance(15.0f);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 2);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.crystalShard;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random rand) {
        float ret;
        if (fortune > 0) {
            float f = (fortune - 1) * rand.nextFloat() - 1f;
            if (f < 0f)
                f = 0f;
            ret = quantityDropped(rand) * (f + 1);
        } else {
            ret = quantityDropped(rand);
        }
        return MathHelper.clamp((int) ret, 1, 64);
    }

    @Override
    public int quantityDropped(Random random) {
        return Config.HEART_CRYSTAL_ORE_QUANTITY_DROPPED;
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        Item item = getItemDropped(world.getBlockState(pos), RANDOM, fortune);
        return item != Item.getItemFromBlock(this) ? 1 + RANDOM.nextInt(5) : 0;
    }
}
