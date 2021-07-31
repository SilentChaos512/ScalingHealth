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

package net.silentchaos512.scalinghealth.utils;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.silentchaos512.scalinghealth.ScalingHealth;

public class SoundUtils {
    public static void play(Player entity, SoundEvent event) {
        play(entity, event, 0.5f, 1 + 0.1f * (float) ScalingHealth.RANDOM.nextGaussian());
    }

    public static void play(Player entity, SoundEvent event, float volume, float pitch) {
        entity.level.playSound(entity, entity.blockPosition(), event, SoundSource.PLAYERS, volume, pitch);
    }
}
