package net.silentchaos512.scalinghealth.client.key;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.silentchaos512.lib.client.key.KeyTrackerSL;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.client.DifficultyDisplayHandler;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;

public class KeyTrackerSH extends KeyTrackerSL {

  public static KeyTrackerSH INSTANCE = new KeyTrackerSH();

  private KeyBinding keyShowDifficultyBar = createBinding("Difficulty Meter - Show",
      KeyConflictContext.IN_GAME, KeyModifier.NONE, Keyboard.KEY_N);
  private KeyBinding keyShowDifficultyBarAlways = createBinding("Difficulty Meter - Show Always",
      KeyConflictContext.IN_GAME, KeyModifier.SHIFT, Keyboard.KEY_N);

  public KeyTrackerSH() {

    super(ScalingHealth.MOD_ID_LOWER);
  }

  @Override
  public void onKeyInput(KeyInputEvent event) {

    if (keyShowDifficultyBarAlways.isPressed()) {
      // Toggle the "Render Difficulty Meter Always" config option.
      ConfigScalingHealth.RENDER_DIFFICULTY_METER_ALWAYS = !ConfigScalingHealth.RENDER_DIFFICULTY_METER_ALWAYS;
      ConfigScalingHealth.save();
    } else if (keyShowDifficultyBar.isPressed()) {
      // Briefly show the difficulty meter.
      DifficultyDisplayHandler.INSTANCE.showBar();
    }
  }

}
