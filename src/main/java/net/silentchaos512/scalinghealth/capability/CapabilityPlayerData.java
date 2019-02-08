package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.silentchaos512.scalinghealth.ScalingHealth;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityPlayerData implements IPlayerData, ICapabilitySerializable<NBTTagCompound> {
    @CapabilityInject(IPlayerData.class)
    public static Capability<IPlayerData> INSTANCE = null;
    public static ResourceLocation NAME = ScalingHealth.res("player_data");

    private static final String NBT_EXTRA_HEARTS = "ExtraHearts";

    private final LazyOptional<IPlayerData> holder = LazyOptional.of(() -> this);

    private int extraHearts;
    private BlockPos lastPos;

    @Override
    public int getExtraHearts() {
        return extraHearts;
    }

    @Override
    public void setExtraHearts(EntityPlayer player, int value) {
        extraHearts = value;
    }

    @Override
    public void tick(EntityPlayer player) {
        // TODO: Position tracking for idle multiplier?

        // TODO: Difficulty by Game Stages

        // TODO: Health by XP

        // TODO: Send update packet to client?
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        return INSTANCE.orEmpty(cap, holder);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInt(NBT_EXTRA_HEARTS, extraHearts);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        extraHearts = nbt.getInt(NBT_EXTRA_HEARTS);
    }

    public static boolean canAttachTo(Entity entity) {
        if (entity.getCapability(INSTANCE).isPresent()) return false;
        return entity instanceof EntityPlayer;
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IPlayerData.class, new Storage(), CapabilityPlayerData::new);
    }

    private static class Storage implements Capability.IStorage<IPlayerData> {
        @Nullable
        @Override
        public INBTBase writeNBT(Capability<IPlayerData> capability, IPlayerData instance, EnumFacing side) {
            if (instance instanceof CapabilityPlayerData) {
                return ((CapabilityPlayerData) instance).serializeNBT();
            }
            return new NBTTagCompound();
        }

        @Override
        public void readNBT(Capability<IPlayerData> capability, IPlayerData instance, EnumFacing side, INBTBase nbt) {
            if (instance instanceof CapabilityPlayerData) {
                ((CapabilityPlayerData) instance).deserializeNBT((NBTTagCompound) nbt);
            }
        }
    }
}
