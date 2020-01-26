package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.silentchaos512.scalinghealth.ScalingHealth;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DifficultySourceCapability implements IDifficultySource, ICapabilitySerializable<CompoundNBT> {
    @CapabilityInject(IDifficultySource.class)
    public static Capability<IDifficultySource> INSTANCE = null;
    public static ResourceLocation NAME = ScalingHealth.getId("difficulty_source");

    private static final String NBT_DIFFICULTY = "Difficulty";

    private final LazyOptional<IDifficultySource> holder = LazyOptional.of(() -> this);

    private float difficulty;
    private boolean exempt = false;

    @Override
    public float getDifficulty() {
        return difficulty;
    }

    @Override
    public void setDifficulty(float value) {
        if(exempt)
            difficulty = 0;
        else
            difficulty = value;
    }

    @Override
    public void setExempt(boolean exempt){
        this.exempt = exempt;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return INSTANCE.orEmpty(cap, holder);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat(NBT_DIFFICULTY, difficulty);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        difficulty = nbt.getFloat(NBT_DIFFICULTY);
    }

    public static boolean canAttachTo(ICapabilityProvider obj) {
        try {
            if (obj.getCapability(INSTANCE).isPresent()) {
                return false;
            }
        } catch (NullPointerException ex) {
            ScalingHealth.LOGGER.error("Failed to get capabilities from {}", obj);
            return false;
        }
        //return (obj instanceof PlayerEntity && Players.isPlayerExempt((PlayerEntity) obj))|| obj instanceof World;
        return  obj instanceof PlayerEntity || obj instanceof World;
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IDifficultySource.class, new Storage(), DifficultySourceCapability::new);
    }

    private static class Storage implements Capability.IStorage<IDifficultySource> {
        @Nullable
        @Override
        public INBT writeNBT(Capability<IDifficultySource> capability, IDifficultySource instance, Direction side) {
            if (instance instanceof DifficultySourceCapability) {
                return ((DifficultySourceCapability) instance).serializeNBT();
            }
            return new CompoundNBT();
        }

        @Override
        public void readNBT(Capability<IDifficultySource> capability, IDifficultySource instance, Direction side, INBT nbt) {
            if (instance instanceof DifficultySourceCapability) {
                ((DifficultySourceCapability) instance).deserializeNBT((CompoundNBT) nbt);
            }
        }
    }
}
