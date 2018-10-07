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

package net.silentchaos512.scalinghealth.compat.gamestages;

import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.silentchaos512.scalinghealth.config.Config;

public class SHGameStagesCompat {
    private SHGameStagesCompat() {}

    public static int getDifficultyFromStages(EntityPlayer player) {
        int max = 0;
        for (String stage : GameStageHelper.getPlayerData(player).getStages()) {
            max = Math.max(max, Config.Difficulty.DIFFICULTY_BY_GAME_STAGES.get(stage));
        }
        return max;
    }
}
