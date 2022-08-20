package net.silentchaos512.scalinghealth.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.utils.ModifierHandler;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;
import net.silentchaos512.scalinghealth.utils.config.SHPlayers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerDataCapability implements IPlayerData, ICapabilitySerializable<CompoundTag> {
    public static Capability<IPlayerData> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});;
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
    public int getBonusHearts(Player player) {
        return SHPlayers.clampedHpFromHeartCrystals(getHeartCrystals()) + SHPlayers.fullHeartsFromXp(player.experienceLevel);
    }

    @Override
    public int getHeartCrystals() {
        return heartCrystals;
    }

    @Override
    public void setHeartCrystals(Player player, int amount) {
        heartCrystals = SHPlayers.clampExtraHearts(amount);
        ModifierHandler.setMaxHealth(player, getModifiedHealth(player), AttributeModifier.Operation.ADDITION);
    }

    @Override
    public int getPowerCrystals() {
        return powerCrystals;
    }

    @Override
    public void setPowerCrystalCount(Player player, int amount) {
        powerCrystals = SHPlayers.clampPowerCrystals(amount);
        ModifierHandler.addAttackDamage(player, getAttackDamageModifier(), AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void updateStats(Player player) {
        ModifierHandler.setMaxHealth(player, getModifiedHealth(player), AttributeModifier.Operation.ADDITION);
        ModifierHandler.addAttackDamage(player, getAttackDamageModifier(), AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void tick(Player player) {
        if (player.level.getGameTime() % 20 == 0 && !player.level.isClientSide) {
            checkPlayerIdle(player);

            if(player instanceof ServerPlayer)
                IPlayerData.sendUpdatePacketTo(player);
        }
    }

    private void checkPlayerIdle(Player player) {
        if (SHDifficulty.areaDifficulty(player.level, player.blockPosition()) >= SHDifficulty.maxValue()) return;

        if (player.blockPosition().equals(lastPos)) {
            timeAfk++;
        }
        else {
            afk = false;
            timeAfk = 0;
        }

        lastPos = player.blockPosition();
        if (timeAfk > SHDifficulty.timeBeforeAfk()) {
            if(!afk) {
                afk = true;
                if(SHDifficulty.afkMessage()) player.sendSystemMessage(Component.translatable("misc.scalinghealth.afkmessage"));
            }
        }

        if (afk) {
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
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(NBT_HEART_CRYSTALS, heartCrystals);
        nbt.putInt(NBT_POWER_CRYSTALS, powerCrystals);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        heartCrystals = nbt.getInt(NBT_HEART_CRYSTALS);
        powerCrystals = nbt.getInt(NBT_POWER_CRYSTALS);
    }

    public static boolean canAttachTo(ICapabilityProvider entity) {
        if (!(entity instanceof Player)) {
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
}
