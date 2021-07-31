package net.silentchaos512.scalinghealth.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.silentchaos512.scalinghealth.event.PlayerBonusRegenHandler;
import net.silentchaos512.scalinghealth.network.ClientSyncMessage;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;
import net.silentchaos512.scalinghealth.utils.config.SHItems;
import net.silentchaos512.scalinghealth.utils.config.SHPlayers;

public interface IPlayerData {
    int getBonusHearts(Player player);

    int getHeartCrystals();

    void setHeartCrystals(Player player, int amount);

    int getPowerCrystals();

    void setPowerCrystalCount(Player player, int amount);

    void updateStats(Player player);

    void tick(Player player);

    default void addHeartCrystals(Player player, int amount) {
        setHeartCrystals(player, getHeartCrystals() + amount);
    }

    default void addPowerCrystal(Player player) {
        setPowerCrystalCount(player, getPowerCrystals() + 1);
    }

    default void addPowerCrystals(Player player, int amount) {
        setPowerCrystalCount(player, getPowerCrystals() + amount);
    }

    default int getModifiedHealth(Player player) {
        return 2 * SHPlayers.clampExtraHearts(getBonusHearts(player)) + SHPlayers.startingHealth() - 20;
    }

    default double getAttackDamageModifier() {
        return getPowerCrystals() * SHItems.powerCrystalIncreaseAmount();
    }

    static void sendUpdatePacketTo(Player player) {
        Level world = player.level;
        BlockPos pos = player.blockPosition();
        ClientSyncMessage msg = new ClientSyncMessage(
                SHDifficulty.source(player).getDifficulty(),
                SHDifficulty.source(world).getDifficulty(),
                (float) SHDifficulty.areaDifficulty(world, pos),
                PlayerBonusRegenHandler.getTimerForPlayer(player),
                SHDifficulty.locationMultiplier(world, pos),
                player.experienceLevel
        );
        ServerPlayer playerMP = (ServerPlayer) player;
        Network.channel.sendTo(msg, playerMP.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
