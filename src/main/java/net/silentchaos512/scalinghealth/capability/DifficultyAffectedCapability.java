package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.event.DifficultyEvents;
import net.silentchaos512.scalinghealth.utils.Difficulty;
import net.silentchaos512.scalinghealth.utils.MobDifficultyHandler;

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
    public void setDifficulty(float value) {
        difficulty = value;
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
            difficulty = (float) Difficulty.areaDifficulty(entity.world, entity.getPosition());
            MobDifficultyHandler.process(entity, this);
            processed = true;

            if (ScalingHealth.LOGGER.isDebugEnabled() && Config.COMMON.debugLogEntitySpawns.get()) {
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

    public static DifficultyAffectedCapability read(PacketBuffer buffer) {
        DifficultyAffectedCapability cap = new DifficultyAffectedCapability();
        cap.blight = buffer.getBoolean(0);
        cap.difficulty = buffer.readFloat();
        cap.processed = true;
        return cap;
    }

    public void write(PacketBuffer buffer) {
        buffer.writeBoolean(blight);
        buffer.writeFloat(difficulty);
    }

    public static boolean canAttachTo(ICapabilityProvider entity) {
        return entity instanceof MobEntity
                && !entity.getCapability(INSTANCE).isPresent()
                && Difficulty.allowsDifficultyChanges((MobEntity) entity);
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
