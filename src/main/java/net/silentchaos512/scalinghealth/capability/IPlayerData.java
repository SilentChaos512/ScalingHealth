package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.silentchaos512.scalinghealth.event.PlayerBonusRegenHandler;
import net.silentchaos512.scalinghealth.network.ClientSyncMessage;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.utils.Difficulty;
import net.silentchaos512.scalinghealth.utils.Players;

public interface IPlayerData {
    int getExtraHearts();

    int getPowerCrystals();

    void setExtraHearts(PlayerEntity player, int amount);

    void setPowerCrystalCount(PlayerEntity player, int amount);

    void updateStats(PlayerEntity player);

    void tick(PlayerEntity player);

    default void addHearts(PlayerEntity player, int amount) {
        setExtraHearts(player, getExtraHearts() + amount);
    }

    default void addPowerCrystal(PlayerEntity player) {
        setPowerCrystalCount(player, getPowerCrystals() + 1);
    }

    default void addPowerCrystals(PlayerEntity player, int amount) {
        setPowerCrystalCount(player, getPowerCrystals() + amount);
    }

    default int getHealthModifier(PlayerEntity player) {
        return 2 * getExtraHearts() + Players.startingHealth(player) - 20;
    }

    default double getAttackDamageModifier(PlayerEntity player) {
        return getPowerCrystals() * Players.powerCrystalIncreaseAmount(player);
    }

    static void sendUpdatePacketTo(PlayerEntity player) {
        World world = player.world;
        BlockPos pos = player.getPosition();
        ClientSyncMessage msg = new ClientSyncMessage(
                Difficulty.source(player).getDifficulty(),
                Difficulty.source(world).getDifficulty(),
                (float) Difficulty.areaDifficulty(world, pos),
                PlayerBonusRegenHandler.getTimerForPlayer(player),
                Difficulty.locationMultiplier(world, pos),
                player.experienceLevel
        );
        ServerPlayerEntity playerMP = (ServerPlayerEntity) player;
        Network.channel.sendTo(msg, playerMP.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }
}
