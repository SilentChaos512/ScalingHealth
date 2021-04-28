package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.silentchaos512.scalinghealth.event.PlayerBonusRegenHandler;
import net.silentchaos512.scalinghealth.network.ClientSyncMessage;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;
import net.silentchaos512.scalinghealth.utils.config.SHItems;
import net.silentchaos512.scalinghealth.utils.config.SHPlayers;

public interface IPlayerData {
    int getBonusHearts(PlayerEntity player);

    int getHeartCrystals();

    void setHeartCrystals(PlayerEntity player, int amount);

    int getPowerCrystals();

    void setPowerCrystalCount(PlayerEntity player, int amount);

    void updateStats(PlayerEntity player);

    void tick(PlayerEntity player);

    default void addHeartCrystals(PlayerEntity player, int amount) {
        setHeartCrystals(player, getHeartCrystals() + amount);
    }

    default void addPowerCrystal(PlayerEntity player) {
        setPowerCrystalCount(player, getPowerCrystals() + 1);
    }

    default void addPowerCrystals(PlayerEntity player, int amount) {
        setPowerCrystalCount(player, getPowerCrystals() + amount);
    }

    default int getModifiedHealth(PlayerEntity player) {
        return 2 * SHPlayers.clampExtraHearts(getBonusHearts(player)) + SHPlayers.startingHealth() - 20;
    }

    default double getAttackDamageModifier() {
        return getPowerCrystals() * SHItems.powerCrystalIncreaseAmount();
    }

    static void sendUpdatePacketTo(PlayerEntity player) {
        World world = player.world;
        BlockPos pos = player.getPosition();
        ClientSyncMessage msg = new ClientSyncMessage(
                SHDifficulty.source(player).getDifficulty(),
                SHDifficulty.source(world).getDifficulty(),
                (float) SHDifficulty.areaDifficulty(world, pos),
                PlayerBonusRegenHandler.getTimerForPlayer(player),
                SHDifficulty.locationMultiplier(world, pos),
                player.experienceLevel
        );
        ServerPlayerEntity playerMP = (ServerPlayerEntity) player;
        Network.channel.sendTo(msg, playerMP.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }
}
