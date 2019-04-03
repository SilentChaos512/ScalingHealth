package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkDirection;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.network.ClientSyncMessage;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.utils.Difficulty;
import net.silentchaos512.scalinghealth.utils.ModifierHandler;
import net.silentchaos512.scalinghealth.utils.Players;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityPlayerData implements IPlayerData, ICapabilitySerializable<NBTTagCompound> {
    @CapabilityInject(IPlayerData.class)
    public static Capability<IPlayerData> INSTANCE = null;
    public static ResourceLocation NAME = ScalingHealth.getId("player_data");

    private static final String NBT_HEART_CRYSTALS = "HeartCrystals";
    private static final String NBT_POWER_CRYSTALS = "PowerCrystals";

    private final LazyOptional<IPlayerData> holder = LazyOptional.of(() -> this);

    private int extraHearts;
    private int powerCrystals;
    private BlockPos lastPos;

    @Override
    public int getExtraHearts() {
        return extraHearts;
    }

    @Override
    public int getPowerCrystals() {
        return powerCrystals;
    }

    @Override
    public void setExtraHearts(EntityPlayer player, int amount) {
        extraHearts = Players.clampExtraHearts(player, amount);
        ModifierHandler.addMaxHealth(player, getHealthModifier(player), 0);
    }

    @Override
    public void setPowerCrystalCount(EntityPlayer player, int amount) {
        powerCrystals = Players.clampPowerCrystals(player, amount);
        ModifierHandler.addAttackDamage(player, getAttackDamageModifier(player), 0);
    }

    @Override
    public void tick(EntityPlayer player) {
        // TODO: Position tracking for idle multiplier?

        // TODO: Difficulty by Game Stages

        // TODO: Health by XP

        if (player.world.getGameTime() % 20 == 0 && player instanceof EntityPlayerMP) {
            sendUpdatePacketTo(player);
        }
    }

    private static void sendUpdatePacketTo(EntityPlayer player) {
        ClientSyncMessage msg = new ClientSyncMessage(
                Difficulty.source(player).getDifficulty(),
                Difficulty.source(player.world).getDifficulty(),
                (float) Difficulty.areaDifficulty(player.world, player.getPosition())
        );
        EntityPlayerMP playerMP = (EntityPlayerMP) player;
        Network.channel.sendTo(msg, playerMP.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        return INSTANCE.orEmpty(cap, holder);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.putInt(NBT_HEART_CRYSTALS, extraHearts);
        nbt.putInt(NBT_POWER_CRYSTALS, powerCrystals);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        extraHearts = nbt.getInt(NBT_HEART_CRYSTALS);
        powerCrystals = nbt.getInt(NBT_POWER_CRYSTALS);
    }

    public static boolean canAttachTo(ICapabilityProvider entity) {
        if (!(entity instanceof EntityPlayer)) {
            return false;
        }
        try {
            if (entity.getCapability(INSTANCE).isPresent()) {
                return false;
            }
        } catch (NullPointerException ex) {
            // Forge seems to be screwing up somewhere?
            ScalingHealth.LOGGER.error("Failed to get capabilities from {}", entity);
            return false;
        }
        return true;
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
