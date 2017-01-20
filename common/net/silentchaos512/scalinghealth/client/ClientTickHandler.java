package net.silentchaos512.scalinghealth.client;

import java.util.ArrayDeque;
import java.util.Queue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.silentchaos512.scalinghealth.init.ModItems;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;

public class ClientTickHandler {

  public static volatile Queue<Runnable> scheduledActions = new ArrayDeque();

  public static int ticksInGame = 0;
  public static float partialTicks = 0f;
  public static float delta = 0f;
  public static float total = 0f;

  private void calcDelta() {

    float oldTotal = total;
    total = ticksInGame + partialTicks;
    delta = total - oldTotal;
  }

  @SubscribeEvent
  public void clientTickEnd(ClientTickEvent event) {

    if (event.phase == Phase.END) {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.world == null) {
        SHPlayerDataHandler.cleanup();
      } else if (mc.player != null) {
        while (!scheduledActions.isEmpty()) {
          scheduledActions.poll().run();
        }
      }

      GuiScreen gui = mc.currentScreen;
      if (gui == null || !gui.doesGuiPauseGame()) {
        ++ticksInGame;
        partialTicks = 0;
      }

      calcDelta();
    }
  }
}
