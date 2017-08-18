package net.silentchaos512.scalinghealth.api;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public class ScalingHealthAPI {

  public double getAreaDifficulty(World world, BlockPos pos) {

    return ConfigScalingHealth.AREA_DIFFICULTY_MODE.getAreaDifficulty(world, pos);
  }

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
}
