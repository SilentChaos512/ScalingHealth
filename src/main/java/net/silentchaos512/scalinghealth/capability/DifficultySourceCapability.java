package net.silentchaos512.scalinghealth.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class DifficultySourceCapability implements IDifficultySource, ICapabilitySerializable<CompoundTag> {
    @CapabilityInject(IDifficultySource.class)
    public static Capability<IDifficultySource> INSTANCE = null;

    public static ResourceLocation NAME = ScalingHealth.getId("difficulty_source");
    private static IDifficultySource overworldCap = null;

    private static final String NBT_DIFFICULTY = "Difficulty";

    private final LazyOptional<IDifficultySource> holder = LazyOptional.of(() -> this);

    private float difficulty;
    private boolean exempt = false;

    public static Optional<IDifficultySource> getOverworldCap(){
        return Optional.ofNullable(overworldCap);
    }

    public static void setOverworldCap(IDifficultySource source){
        overworldCap = source;
    }

    @Override
    public float getDifficulty() {
        return exempt ? 0 : difficulty;
    }

    @Override
    public void setDifficulty(float value) {
        difficulty = (float) SHDifficulty.clamp(value);
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
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putFloat(NBT_DIFFICULTY, difficulty);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        difficulty = nbt.getFloat(NBT_DIFFICULTY);
    }

    //TODO remove try-catch? also not attaching to client world?
    public static boolean canAttachTo(ICapabilityProvider obj) {
        try {
            if (obj.getCapability(INSTANCE).isPresent()) {
                return false;
            }
        } catch (NullPointerException ex) {
            ScalingHealth.LOGGER.error("Failed to get capabilities from {}", obj);
            return false;
        }
        return obj instanceof Player || (obj instanceof ServerLevel && ((ServerLevel) obj).dimension().equals(Level.OVERWORLD));
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IDifficultySource.class);
    }
}
