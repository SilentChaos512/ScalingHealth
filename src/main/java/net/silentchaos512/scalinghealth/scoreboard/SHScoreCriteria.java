package net.silentchaos512.scalinghealth.scoreboard;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.silentchaos512.scalinghealth.ScalingHealth;

public class SHScoreCriteria {

  public static IScoreCriteria difficulty = new ScoreCriteria(
      ScalingHealth.RESOURCE_PREFIX + "difficulty");

  public static void updateScore(EntityPlayer player, int amount) {

    for (ScoreObjective scoreobjective : player.getWorldScoreboard()
        .getObjectivesFromCriteria(SHScoreCriteria.difficulty)) {
      Score score = player.getWorldScoreboard().getOrCreateScore(player.getName(),
          scoreobjective);
      score.setScorePoints(amount);
    }
  }
}
