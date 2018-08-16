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

package net.silentchaos512.scalinghealth.compat.morpheus;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.quetzi.morpheus.Morpheus;
import net.quetzi.morpheus.MorpheusRegistry;
import net.quetzi.morpheus.api.INewDayHandler;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;
import net.silentchaos512.scalinghealth.world.ScalingHealthSavedData;

public class SHMorpheusCompat {

  public static class NewDayHandler implements INewDayHandler {

    /**
     * Seems like there can only be one INewDayHandler per dimension? So, store a reference to the original one and pass
     * control in startNewDay to preserve whatever behavior it had.
     */
    INewDayHandler parent;

    public NewDayHandler(INewDayHandler parent) {

      this.parent = parent;
    }

    @Override
    public void startNewDay() {

      if (parent != null)
        parent.startNewDay();

      // Get all players in the world and increase their difficulty.
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server == null)
        return;

      World world = server.worlds[0];
      if (world == null)
        return;

      for (EntityPlayer player : world.getPlayers(EntityPlayer.class, e -> true)) {
        PlayerData data = SHPlayerDataHandler.get(player);
        if (data != null) {
          data.incrementDifficulty(Config.DIFFICULTY_FOR_SLEEPING, false);
        }
      }

      ScalingHealthSavedData data = ScalingHealthSavedData.get(world);
      if (data != null) {
        data.difficulty += Config.DIFFICULTY_FOR_SLEEPING;
      }
    }
  }

  public static void init() {

    INewDayHandler parent = null;
    if (Morpheus.register.isDimRegistered(0))
      parent = MorpheusRegistry.registry.get(0);

    INewDayHandler newHandler = new NewDayHandler(parent);
    ScalingHealth.logHelper.info("Replacing Morpheus new day handler for dimension {}!", 0);
    ScalingHealth.logHelper.info("Parent handler: {}", parent);
    Morpheus.register.registerHandler(newHandler, 0);
  }
}
