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

package net.silentchaos512.scalinghealth.scoreboard;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.silentchaos512.scalinghealth.ScalingHealth;

public class SHScoreCriteria {
    public static IScoreCriteria difficulty = new ScoreCriteria(ScalingHealth.RESOURCE_PREFIX + "difficulty");

    public static void updateScore(EntityPlayer player, int amount) {
        for (ScoreObjective scoreobjective : player.getWorldScoreboard().getObjectivesFromCriteria(SHScoreCriteria.difficulty)) {
            Score score = player.getWorldScoreboard().getOrCreateScore(player.getName(), scoreobjective);
            score.setScorePoints(amount);
        }
    }
}
