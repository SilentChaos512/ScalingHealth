package net.silentchaos512.scalinghealth.lib;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public enum EnumAreaDifficultyMode {

  WEIGHTED_AVERAGE, AVERAGE, MIN_LEVEL, MAX_LEVEL, DISTANCE_FROM_SPAWN, DISTANCE_FROM_ORIGIN;

  private boolean unknownModeReported = false;

  public static EnumAreaDifficultyMode loadFromConfig(Configuration c,
      EnumAreaDifficultyMode defaultValue) {

    String[] validValues = new String[values().length];
    for (int i = 0; i < values().length; ++i)
      validValues[i] = values()[i].name();

    //@formatter:off
    String str = c.getString("Area Mode", ConfigScalingHealth.CAT_DIFFICULTY,
        defaultValue.name(),
        "Defines how the area difficulty is determined when spawning a mob.\n"
        + "  AVERAGE - The average difficulty level of all nearby players.\n"
        + "  WEIGHTED_AVERAGE - Similar to average, but closer players have a greater impact on difficulty.\n"
        + "  MIN_LEVEL - The lowest difficulty level of all nearby players.\n"
        + "  MAX_LEVEL - The highest difficulty level of all nearby players.\n"
        + "  DISTANCE_FROM_SPAWN - Based on the mob's distance from spawn.\n"
        + "  DISTANCE_FROM_ORIGIN - Based on the mob's distance from the origin.",
        validValues);
    //@formatter:on

    for (EnumAreaDifficultyMode mode : values())
      if (mode.name().equals(str))
        return mode;
    return defaultValue;
  }

  public double getAreaDifficulty(World world, BlockPos pos) {

    // Get players in range. TODO: Only get player list for types that need it?
    int radius = ConfigScalingHealth.DIFFICULTY_SEARCH_RADIUS;
    final long radiusSquared = radius <= 0 ? Long.MAX_VALUE : radius * radius;
    radius = radius <= 0 ? Integer.MAX_VALUE : radius;
    List<EntityPlayer> players = world.getPlayers(EntityPlayer.class,
        p -> p.getDistanceSq(pos) <= radiusSquared);

    if (players.size() == 0)
      return 0;

    double total = 0;
    BlockPos origin = BlockPos.ORIGIN;

    switch (this) {
      case WEIGHTED_AVERAGE:
        int totalWeight = 0;
        for (EntityPlayer player : players) {
          PlayerData data = SHPlayerDataHandler.get(player);
          if (data != null) {
            int distance = (int) pos.getDistance((int) player.posX, pos.getY(), (int) player.posZ);
            int weight = (int) (radius - distance) / 16 + 1;

            total += weight * data.getDifficulty();
            totalWeight += weight;
          }
        }
        return totalWeight <= 0 ? 0 : total / totalWeight;

      case AVERAGE:
        for (EntityPlayer player : players) {
          PlayerData data = SHPlayerDataHandler.get(player);
          if (data != null)
            total += data.getDifficulty();
        }
        return total / players.size();

      case MAX_LEVEL:
        double max = 0;
        for (EntityPlayer player : players) {
          PlayerData data = SHPlayerDataHandler.get(player);
          if (data != null) {
            double d = data.getDifficulty();
            max = Math.max(d, max);
          }
        }
        return max;

      case MIN_LEVEL:
        double min = ConfigScalingHealth.DIFFICULTY_MAX;
        for (EntityPlayer player : players) {
          PlayerData data = SHPlayerDataHandler.get(player);
          if (data != null) {
            double d = data.getDifficulty();
            min = Math.min(d, min);
          }
        }
        return min;

      case DISTANCE_FROM_SPAWN:
        origin = world.getSpawnPoint();
      case DISTANCE_FROM_ORIGIN:
        int dx = pos.getX() - origin.getX();
        int dz = pos.getZ() - origin.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        return distance * ConfigScalingHealth.DIFFICULTY_PER_BLOCK;
    }

    return 0;
  }
}
