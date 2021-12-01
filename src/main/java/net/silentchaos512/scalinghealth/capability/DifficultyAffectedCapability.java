package net.silentchaos512.scalinghealth.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
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

public class DifficultyAffectedCapability implements IDifficultyAffected, ICapabilitySerializable<CompoundTag> {
    public static Capability<IDifficultyAffected> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});;
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
    public void setDifficulty(Mob mob) {
        difficulty = (float) ((Math.random()*(0.1)+0.95) * SHDifficulty.areaDifficulty(mob.level, mob.blockPosition()));
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
    public void tick(Mob entity) {
        if (!processed && entity.isAlive() && entity.tickCount > 2) {
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
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean(NBT_BLIGHT, blight);
        nbt.putFloat(NBT_DIFFICULTY, difficulty);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        blight = nbt.getBoolean(NBT_BLIGHT);
        difficulty = nbt.getFloat(NBT_DIFFICULTY);
    }

    public static boolean canAttachTo(ICapabilityProvider entity) {
        return entity instanceof Mob
                && !entity.getCapability(INSTANCE).isPresent()
                && SHMobs.allowsDifficultyChanges((Mob) entity);
    }
}
