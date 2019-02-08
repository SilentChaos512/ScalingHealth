package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.silentchaos512.scalinghealth.ScalingHealth;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityDifficultySource implements IDifficultySource, ICapabilitySerializable<NBTTagCompound> {
    @CapabilityInject(IDifficultySource.class)
    public static Capability<IDifficultySource> INSTANCE = null;
    public static ResourceLocation NAME = ScalingHealth.res("difficulty_source");

    private static final String NBT_DIFFICULTY = "Difficulty";

    private final LazyOptional<IDifficultySource> holder = LazyOptional.of(() -> this);

    private float difficulty;

    @Override
    public float getDifficulty() {
        return difficulty;
    }

    @Override
    public void setDifficulty(float value) {
        difficulty = value;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        return INSTANCE.orEmpty(cap, holder);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setFloat(NBT_DIFFICULTY, difficulty);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        difficulty = nbt.getFloat(NBT_DIFFICULTY);
    }

    public static boolean canAttachTo(ICapabilityProvider obj) {
        if (obj.getCapability(INSTANCE).isPresent()) return false;
        return obj instanceof EntityPlayer || obj instanceof World;
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IDifficultySource.class, new Storage(), CapabilityDifficultySource::new);
    }

    private static class Storage implements Capability.IStorage<IDifficultySource> {
        @Nullable
        @Override
        public INBTBase writeNBT(Capability<IDifficultySource> capability, IDifficultySource instance, EnumFacing side) {
            if (instance instanceof CapabilityDifficultySource) {
                return ((CapabilityDifficultySource) instance).serializeNBT();
            }
            return new NBTTagCompound();
        }

        @Override
        public void readNBT(Capability<IDifficultySource> capability, IDifficultySource instance, EnumFacing side, INBTBase nbt) {
            if (instance instanceof CapabilityDifficultySource) {
                ((CapabilityDifficultySource) instance).deserializeNBT((NBTTagCompound) nbt);
            }
        }
    }
}
