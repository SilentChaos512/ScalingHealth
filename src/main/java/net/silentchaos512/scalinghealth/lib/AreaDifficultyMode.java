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

import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.capability.IDifficultySource;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.DimensionConfig;
import net.silentchaos512.scalinghealth.utils.Difficulty;

import java.util.Collection;
import java.util.Locale;

public enum AreaDifficultyMode {
    WEIGHTED_AVERAGE {
        @Override
        public double getBaseAreaDifficulty(World world, BlockPos pos, int radius) {
            double total = 0;
            int totalWeight = 0;
            for (Tuple<BlockPos, IDifficultySource> tuple : Difficulty.sources(world, pos, radius)) {
                BlockPos sourcePos = tuple.getA();
                IDifficultySource source = tuple.getB();
                int distance = (int) pos.getDistance(sourcePos);
                int weight = (radius - distance) / 16 + 1;
                total += weight * source.getDifficulty();
                totalWeight += weight;
            }
            return totalWeight <= 0 ? 0 : total / totalWeight;
        }
    },
    AVERAGE {
        @Override
        public double getBaseAreaDifficulty(World world, BlockPos pos, int radius) {
            double total = 0;
            Collection<Tuple<BlockPos, IDifficultySource>> sources = Difficulty.sources(world, pos, radius);
            for (Tuple<BlockPos, IDifficultySource> tuple : sources) {
                total += tuple.getB().getDifficulty();
            }
            return total / sources.size();
        }
    },
    MIN_LEVEL {
        @Override
        public double getBaseAreaDifficulty(World world, BlockPos pos, int radius) {
            double min = Difficulty.maxValue(world);
            for (Tuple<BlockPos, IDifficultySource> tuple : Difficulty.sources(world, pos, radius)) {
                min = Math.min(tuple.getB().getDifficulty(), min);
            }
            return min;
        }
    },
    MAX_LEVEL {
        @Override
        public double getBaseAreaDifficulty(World world, BlockPos pos, int radius) {
            double max = 0;
            for (Tuple<BlockPos, IDifficultySource> tuple : Difficulty.sources(world, pos, radius)) {
                max = Math.max(tuple.getB().getDifficulty(), max);
            }
            return max;
        }
    },
    DISTANCE_FROM_SPAWN {
        @Override
        public double getBaseAreaDifficulty(World world, BlockPos pos, int radius) {
            double dx = pos.getX() - pos.getX();
            double dz = pos.getZ() - pos.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            return distance * Difficulty.distanceFactor(world);
        }
    },
    DISTANCE_FROM_ORIGIN {
        @Override
        public double getBaseAreaDifficulty(World world, BlockPos pos, int radius) {
            double dx = pos.getX() - pos.getX();
            double dz = pos.getZ() - pos.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            return distance * Difficulty.distanceFactor(world);
        }
    },
    DISTANCE_AND_TIME {
        @Override
        public double getBaseAreaDifficulty(World world, BlockPos pos, int radius) {
            double fromSources = WEIGHTED_AVERAGE.getBaseAreaDifficulty(world, pos, radius);
            double fromDistance = DISTANCE_FROM_SPAWN.getBaseAreaDifficulty(world, pos, radius);
            return fromSources + fromDistance;
        }
    },
    DIMENSION_WIDE {
        @Override
        public double getBaseAreaDifficulty(World world, BlockPos pos, int radius) {
            return Difficulty.source(world).getDifficulty();
        }
    };

    public abstract double getBaseAreaDifficulty(World world, BlockPos pos, int radius);

    public double getAreaDifficulty(World world, BlockPos pos, boolean groupBonus) {
        // Is difficulty disabled via game rule or other means?
        if (!world.isRemote && !Difficulty.enabledIn(world)) return 0.0;

        final int radius = Difficulty.searchRadius(world);
        final double baseAreaDifficulty = getBaseAreaDifficulty(world, pos, radius);
        DimensionConfig config = Config.get(world);
        final double locationMultiplier = config.difficulty.getLocationMultiplier(world, pos);
        final double clampedDifficulty = Difficulty.clamp(world, baseAreaDifficulty * locationMultiplier);

        return groupBonus ? Difficulty.withGroupBonus(world, pos, clampedDifficulty) : clampedDifficulty;
    }

    public ITextComponent getDisplayName() {
        return new TextComponentTranslation("config.scalinghealth.area_mode." + name().toLowerCase(Locale.ROOT));
    }

    public static AreaDifficultyMode fromOrdinal(int ordinal) {
        return values()[MathHelper.clamp(ordinal, 0, values().length - 1)];
    }
}
