package net.silentchaos512.scalinghealth.compat.morpheus;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.quetzi.morpheus.Morpheus;
import net.quetzi.morpheus.MorpheusRegistry;
import net.quetzi.morpheus.api.INewDayHandler;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

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
          data.incrementDifficulty(ConfigScalingHealth.DIFFICULTY_FOR_SLEEPING);
        }
      }
    }
  }

  public static void init() {

    INewDayHandler parent = null;
    if (Morpheus.register.isDimRegistered(0))
      parent = MorpheusRegistry.registry.get(0);

    INewDayHandler newHandler = new NewDayHandler(parent);
    ScalingHealth.logHelper.info("Replacing Morpheus new day handler for dimension 0!");
    ScalingHealth.logHelper.info("Parent handler: " + parent);
    Morpheus.register.registerHandler(newHandler, 0);
  }
}
