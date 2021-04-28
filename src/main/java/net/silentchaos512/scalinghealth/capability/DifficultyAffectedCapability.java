package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.SHConfig;
import net.silentchaos512.scalinghealth.event.DifficultyEvents;
import net.silentchaos512.scalinghealth.utils.MobDifficultyHandler;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;
import net.silentchaos512.scalinghealth.utils.config.SHMobs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DifficultyAffectedCapability implements IDifficultyAffected, ICapabilitySerializable<CompoundNBT> {
    @CapabilityInject(IDifficultyAffected.class)
    public static Capability<IDifficultyAffected> INSTANCE = null;
    public static ResourceLocation NAME = ScalingHealth.getId("difficulty_affected");

    private static final String NBT_BLIGHT = "Blight";
    private static final String NBT_DIFFICULTY = "Difficulty";

    private final LazyOptional<IDifficultyAffected> holder = LazyOptional.of(() -> this);

    private float difficulty;
    private boolean blight;
    private boolean processed;

    @Override
    public float getDifficulty() {
        return difficulty;
    }

    @Override
    public void setDifficulty(MobEntity mob) {
        difficulty = (float) ((Math.random()*(0.1)+0.95) * SHDifficulty.areaDifficulty(mob.world, mob.getPosition()));
    }

    @Override
    public void forceDifficulty(float diff) {
        difficulty = diff;
    }

    @Override
    public boolean isBlight() {
        return blight;
    }

    @Override
    public void setIsBlight(boolean value) {
        blight = value;
    }

    @Override
    public void setProcessed(boolean value) {
        this.processed = value;
    }

    @Override
    public void tick(MobEntity entity) {
        if (!processed && entity.isAlive() && entity.ticksExisted > 2) {
            setDifficulty(entity);
            MobDifficultyHandler.process(entity, this);
            processed = true;

            if (ScalingHealth.LOGGER.isDebugEnabled() && SHConfig.SERVER.debugLogEntitySpawns.get()) {
                ScalingHealth.LOGGER.debug(DifficultyEvents.MARKER, "Processed {} -> difficulty={}, isBlight={}", entity, difficulty, blight);
            }
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return INSTANCE.orEmpty(cap, holder);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean(NBT_BLIGHT, blight);
        nbt.putFloat(NBT_DIFFICULTY, difficulty);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        blight = nbt.getBoolean(NBT_BLIGHT);
        difficulty = nbt.getFloat(NBT_DIFFICULTY);
    }

    public static boolean canAttachTo(ICapabilityProvider entity) {
        return entity instanceof MobEntity
                && !entity.getCapability(INSTANCE).isPresent()
                && SHMobs.allowsDifficultyChanges((MobEntity) entity);
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IDifficultyAffected.class, new Storage(), DifficultyAffectedCapability::new);
    }

    private static class Storage implements Capability.IStorage<IDifficultyAffected> {
        @Nullable
        @Override
        public INBT writeNBT(Capability<IDifficultyAffected> capability, IDifficultyAffected instance, Direction side) {
            if (instance instanceof DifficultyAffectedCapability) {
                return ((DifficultyAffectedCapability) instance).serializeNBT();
            }
            return new CompoundNBT();
        }

        @Override
        public void readNBT(Capability<IDifficultyAffected> capability, IDifficultyAffected instance, Direction side, INBT nbt) {
            if (instance instanceof DifficultyAffectedCapability) {
                ((DifficultyAffectedCapability) instance).deserializeNBT((CompoundNBT) nbt);
            }
        }
    }
}
