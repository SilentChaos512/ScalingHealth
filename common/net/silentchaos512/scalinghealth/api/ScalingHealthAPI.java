package net.silentchaos512.scalinghealth.api;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public class ScalingHealthAPI {

  /**
   * Gets the area difficulty for the given position.
   * 
   * @return The area difficulty.
   */
  public double getAreaDifficulty(World world, BlockPos pos) {

    return ConfigScalingHealth.AREA_DIFFICULTY_MODE.getAreaDifficulty(world, pos);
  }

  /**
   * Gets the player difficulty for the given player.
   * 
   * @return The player's difficulty, or Double.NaN if the data can't be obtained for some reason.
   */
  public double getPlayerDifficulty(@Nonnull EntityPlayer player) {

    if (player == null) {
      return Double.NaN;
    }

    PlayerData data = SHPlayerDataHandler.get(player);
    if (data == null) {
      return Double.NaN;
    }

    return data.getDifficulty();
  }

  /**
   * Adds difficulty to the player. The player's difficulty will be clamped to valid values.
   */
  public void addPlayerDifficulty(@Nonnull EntityPlayer player, double amount) {

    if (player != null) {
      PlayerData data = SHPlayerDataHandler.get(player);
      if (data != null) {
        data.incrementDifficulty(amount);
      }
    }
  }
}
