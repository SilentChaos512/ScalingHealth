package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.silentchaos512.scalinghealth.ScalingHealth;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.naming.spi.ResolveResult;

public class CapabilityDifficultyAffected implements IDifficultyAffected, ICapabilitySerializable<NBTTagCompound> {
    @CapabilityInject(IDifficultyAffected.class)
    public static Capability<IDifficultyAffected> INSTANCE = null;
    public static ResourceLocation NAME = ScalingHealth.res("difficulty_affected");

    private static final String NBT_DIFFICULTY = "Difficulty";

    private final LazyOptional<IDifficultyAffected> holder = LazyOptional.of(() -> this);

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

    public static boolean canAttachTo(Entity entity) {
        return !entity.getCapability(INSTANCE).isPresent()
                && entity instanceof EntityLivingBase
                && !(entity instanceof EntityPlayer);
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IDifficultyAffected.class, new Storage(), CapabilityDifficultyAffected::new);
    }

    private static class Storage implements Capability.IStorage<IDifficultyAffected> {
        @Nullable
        @Override
        public INBTBase writeNBT(Capability<IDifficultyAffected> capability, IDifficultyAffected instance, EnumFacing side) {
            if (instance instanceof CapabilityDifficultyAffected) {
                return ((CapabilityDifficultyAffected) instance).serializeNBT();
            }
            return new NBTTagCompound();
        }

        @Override
        public void readNBT(Capability<IDifficultyAffected> capability, IDifficultyAffected instance, EnumFacing side, INBTBase nbt) {
            if (instance instanceof CapabilityDifficultyAffected) {
                ((CapabilityDifficultyAffected) instance).deserializeNBT((NBTTagCompound) nbt);
            }
        }
    }
}
