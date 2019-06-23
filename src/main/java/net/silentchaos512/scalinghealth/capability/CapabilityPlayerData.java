package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkDirection;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.event.PlayerBonusRegenHandler;
import net.silentchaos512.scalinghealth.network.ClientSyncMessage;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.utils.Difficulty;
import net.silentchaos512.scalinghealth.utils.ModifierHandler;
import net.silentchaos512.scalinghealth.utils.Players;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityPlayerData implements IPlayerData, ICapabilitySerializable<CompoundNBT> {
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
    public void setExtraHearts(PlayerEntity player, int amount) {
        extraHearts = Players.clampExtraHearts(player, amount);
        ModifierHandler.addMaxHealth(player, getHealthModifier(player), AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void setPowerCrystalCount(PlayerEntity player, int amount) {
        powerCrystals = Players.clampPowerCrystals(player, amount);
        ModifierHandler.addAttackDamage(player, getAttackDamageModifier(player), AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void tick(PlayerEntity player) {
        // TODO: Position tracking for idle multiplier?

        // TODO: Difficulty by Game Stages

        // TODO: Health by XP

        if (player.world.getGameTime() % 20 == 0 && player instanceof ServerPlayerEntity) {
            sendUpdatePacketTo(player);
        }
    }

    private static void sendUpdatePacketTo(PlayerEntity player) {
        World world = player.world;
        BlockPos pos = player.getPosition();
        ClientSyncMessage msg = new ClientSyncMessage(
                Difficulty.source(player).getDifficulty(),
                Difficulty.source(world).getDifficulty(),
                (float) Difficulty.areaDifficulty(world, pos),
                PlayerBonusRegenHandler.getTimerForPlayer(player),
                Difficulty.locationMultiplier(world, pos)
        );
        ServerPlayerEntity playerMP = (ServerPlayerEntity) player;
        Network.channel.sendTo(msg, playerMP.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return INSTANCE.orEmpty(cap, holder);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt(NBT_HEART_CRYSTALS, extraHearts);
        nbt.putInt(NBT_POWER_CRYSTALS, powerCrystals);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        extraHearts = nbt.getInt(NBT_HEART_CRYSTALS);
        powerCrystals = nbt.getInt(NBT_POWER_CRYSTALS);
    }

    public static boolean canAttachTo(ICapabilityProvider entity) {
        if (!(entity instanceof PlayerEntity)) {
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
        public INBT writeNBT(Capability<IPlayerData> capability, IPlayerData instance, Direction side) {
            if (instance instanceof CapabilityPlayerData) {
                return ((CapabilityPlayerData) instance).serializeNBT();
            }
            return new CompoundNBT();
        }

        @Override
        public void readNBT(Capability<IPlayerData> capability, IPlayerData instance, Direction side, INBT nbt) {
            if (instance instanceof CapabilityPlayerData) {
                ((CapabilityPlayerData) instance).deserializeNBT((CompoundNBT) nbt);
            }
        }
    }
}
