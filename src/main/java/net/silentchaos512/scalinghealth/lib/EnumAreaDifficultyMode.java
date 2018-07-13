/*
 * Scaling Health
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.scalinghealth.lib;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;
import net.silentchaos512.scalinghealth.world.ScalingHealthSavedData;

import java.util.List;

public enum EnumAreaDifficultyMode {

  WEIGHTED_AVERAGE,
  AVERAGE,
  MIN_LEVEL,
  MAX_LEVEL,
  DISTANCE_FROM_SPAWN,
  DISTANCE_FROM_ORIGIN,
  DISTANCE_AND_TIME,
  SERVER_WIDE;

  public static EnumAreaDifficultyMode loadFromConfig(Configuration c,
      EnumAreaDifficultyMode defaultValue) {

    String[] validValues = new String[values().length];
    for (int i = 0; i < values().length; ++i)
      validValues[i] = values()[i].name();

    //@formatter:off
    String str = c.getString("Area Mode", Config.CAT_DIFFICULTY,
        defaultValue.name(),
        "Defines how the area difficulty is determined when spawning a mob.\n"
        + "  AVERAGE - The average difficulty level of all nearby players.\n"
        + "  WEIGHTED_AVERAGE - Similar to average, but closer players have a greater impact on difficulty.\n"
        + "  MIN_LEVEL - The lowest difficulty level of all nearby players.\n"
        + "  MAX_LEVEL - The highest difficulty level of all nearby players.\n"
        + "  DISTANCE_FROM_SPAWN - Based on the mob's distance from spawn.\n"
        + "  DISTANCE_FROM_ORIGIN - Based on the mob's distance from the origin.\n"
        + "  DISTANCE_AND_TIME - Mix of DISTANCE_FROM_SPAWN and WEIGHTED_AVERAGE.\n"
        + "  SERVER_WIDE - Difficulty is tracked at a server level, individual player difficulty has no impact.",
        validValues);
    //@formatter:on

    for (EnumAreaDifficultyMode mode : values())
      if (mode.name().equalsIgnoreCase(str))
        return mode;
    return defaultValue;
  }

  public double getAreaDifficulty(World world, BlockPos pos) {

    return getAreaDifficulty(world, pos, true);
  }

  public double getAreaDifficulty(World world, BlockPos pos, boolean addGroupBonus) {

    return getAreaDifficulty(world, pos, addGroupBonus, true);
  }

  public double getAreaDifficulty(World world, BlockPos pos, boolean addGroupBonus,
      boolean clampValue) {

    if (!world.isRemote && !world.getGameRules().getBoolean(ScalingHealth.GAME_RULE_DIFFICULTY)) {
      // Difficulty is disabled via game rule.
      return 0.0;
    }

    // Get players in range. TODO: Only get player list for types that need it?
    int radius = Config.DIFFICULTY_SEARCH_RADIUS;
    final long radiusSquared = radius <= 0 ? Long.MAX_VALUE : radius * radius;
    radius = radius <= 0 ? Integer.MAX_VALUE : radius;
    List<EntityPlayer> players = world.getPlayers(EntityPlayer.class,
        p -> p.getDistanceSq(pos) <= radiusSquared);

    if (players.size() == 0)
      return 0;

    double total = 0;
    double ret = 0;
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
        ret = totalWeight <= 0 ? 0 : total / totalWeight;
        break;

      case AVERAGE:
        for (EntityPlayer player : players) {
          PlayerData data = SHPlayerDataHandler.get(player);
          if (data != null)
            total += data.getDifficulty();
        }
        ret = total / players.size();
        break;

      case MAX_LEVEL:
        double max = 0;
        for (EntityPlayer player : players) {
          PlayerData data = SHPlayerDataHandler.get(player);
          if (data != null) {
            double d = data.getDifficulty();
            max = Math.max(d, max);
          }
        }
        ret = max;
        break;

      case MIN_LEVEL:
        double min = Config.DIFFICULTY_MAX;
        for (EntityPlayer player : players) {
          PlayerData data = SHPlayerDataHandler.get(player);
          if (data != null) {
            double d = data.getDifficulty();
            min = Math.min(d, min);
          }
        }
        ret = min;
        break;

      case DISTANCE_FROM_SPAWN:
        origin = world.getSpawnPoint();
      case DISTANCE_FROM_ORIGIN:
        double dx = pos.getX() - origin.getX();
        double dz = pos.getZ() - origin.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        ret = distance * Config.DIFFICULTY_PER_BLOCK;
        break;

      case DISTANCE_AND_TIME:
        double diffFromPlayers = WEIGHTED_AVERAGE.getAreaDifficulty(world, pos, false, false);
        double diffFromDistance = DISTANCE_FROM_SPAWN.getAreaDifficulty(world, pos, false, false);
        ret = diffFromPlayers + diffFromDistance;
        break;
      case SERVER_WIDE:
        ScalingHealthSavedData data = ScalingHealthSavedData.get(world);
        if (data != null)
          ret = data.difficulty;
        break;
    }

    // Clamp to difficulty range (intentionally done before group bonus)
    if (clampValue)
      ret = MathHelper.clamp(ret, Config.DIFFICULTY_MIN,
          Config.DIFFICULTY_MAX);

    // Group bonus?
    if (addGroupBonus)
      ret *= 1 + Config.DIFFICULTY_GROUP_AREA_BONUS * (players.size() - 1);

    return ret;
  }
}
