package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.utils.ModifierHandler;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;
import net.silentchaos512.scalinghealth.utils.config.SHPlayers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerDataCapability implements IPlayerData, ICapabilitySerializable<CompoundNBT> {
    @CapabilityInject(IPlayerData.class)
    public static Capability<IPlayerData> INSTANCE = null;
    public static ResourceLocation NAME = ScalingHealth.getId("player_data");

    private static final String NBT_HEART_CRYSTALS = "HeartCrystals";
    private static final String NBT_POWER_CRYSTALS = "PowerCrystals";

    private final LazyOptional<IPlayerData> holder = LazyOptional.of(() -> this);

    private boolean afk = false;
    private int timeAfk = 0;
    private BlockPos lastPos;

    private int heartCrystals;
    private int powerCrystals;

    @Override
    public int getBonusHearts(PlayerEntity player) {
        return SHPlayers.clampedHpFromHeartCrystals(getHeartCrystals()) + SHPlayers.fullHeartsFromXp(player.experienceLevel);
    }

    @Override
    public int getHeartCrystals() {
        return heartCrystals;
    }

    @Override
    public void setHeartCrystals(PlayerEntity player, int amount) {
        heartCrystals = SHPlayers.clampExtraHearts(amount);
        ModifierHandler.setMaxHealth(player, getModifiedHealth(player), AttributeModifier.Operation.ADDITION);
    }

    @Override
    public int getPowerCrystals() {
        return powerCrystals;
    }

    @Override
    public void setPowerCrystalCount(PlayerEntity player, int amount) {
        powerCrystals = SHPlayers.clampPowerCrystals(amount);
        ModifierHandler.addAttackDamage(player, getAttackDamageModifier(), AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void updateStats(PlayerEntity player) {
        ModifierHandler.setMaxHealth(player, getModifiedHealth(player), AttributeModifier.Operation.ADDITION);
        ModifierHandler.addAttackDamage(player, getAttackDamageModifier(), AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void tick(PlayerEntity player) {
        if(player.level.getGameTime() % 20 == 0 && !player.level.isClientSide){
            checkPlayerIdle(player);

            if(player instanceof ServerPlayerEntity)
                IPlayerData.sendUpdatePacketTo(player);
        }
    }

    private void checkPlayerIdle(PlayerEntity player) {
        if(SHDifficulty.areaDifficulty(player.level, player.blockPosition()) >= SHDifficulty.maxValue()) return;

        if(player.blockPosition().equals(lastPos)){
            timeAfk++;
        }
        else {
            afk = false;
            timeAfk = 0;
        }

        lastPos = player.blockPosition();
        if(timeAfk > SHDifficulty.timeBeforeAfk()){
            if(!afk) {
                afk = true;
                if(SHDifficulty.afkMessage()) player.sendMessage(new TranslationTextComponent("misc.scalinghealth.afkmessage"), Util.NIL_UUID);
            }
        }

        if(afk) {
            IDifficultySource data = SHDifficulty.source(player);
            float changePerSec = (float) SHDifficulty.changePerSecond();
            //since last second we added "changePerSec" difficulty, we subtract an amount based on idlemodifier
            data.addDifficulty(- changePerSec * (float) (1 - SHDifficulty.idleModifier()));
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
        nbt.putInt(NBT_HEART_CRYSTALS, heartCrystals);
        nbt.putInt(NBT_POWER_CRYSTALS, powerCrystals);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        heartCrystals = nbt.getInt(NBT_HEART_CRYSTALS);
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
        CapabilityManager.INSTANCE.register(IPlayerData.class, new Storage(), PlayerDataCapability::new);
    }

    private static class Storage implements Capability.IStorage<IPlayerData> {
        @Nullable
        @Override
        public INBT writeNBT(Capability<IPlayerData> capability, IPlayerData instance, Direction side) {
            if (instance instanceof PlayerDataCapability) {
                return ((PlayerDataCapability) instance).serializeNBT();
            }
            return new CompoundNBT();
        }

        @Override
        public void readNBT(Capability<IPlayerData> capability, IPlayerData instance, Direction side, INBT nbt) {
            if (instance instanceof PlayerDataCapability) {
                ((PlayerDataCapability) instance).deserializeNBT((CompoundNBT) nbt);
            }
        }
    }
}
