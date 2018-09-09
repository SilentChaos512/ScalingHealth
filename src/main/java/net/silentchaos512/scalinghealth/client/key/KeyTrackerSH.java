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

package net.silentchaos512.scalinghealth.client.key;

import net.silentchaos512.scalinghealth.config.Config;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.silentchaos512.lib.client.key.KeyTrackerSL;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.client.DifficultyDisplayHandler;

public class KeyTrackerSH extends KeyTrackerSL {
    public static final KeyTrackerSH INSTANCE = new KeyTrackerSH();

    private KeyBinding keyShowDifficultyBar = createBinding("Difficulty Meter - Show",
            KeyConflictContext.IN_GAME, KeyModifier.NONE, Keyboard.KEY_N);
    private KeyBinding keyShowDifficultyBarAlways = createBinding("Difficulty Meter - Show Always",
            KeyConflictContext.IN_GAME, KeyModifier.SHIFT, Keyboard.KEY_N);

    private KeyTrackerSH() {
        super(ScalingHealth.MOD_ID_LOWER);
    }

    @Override
    public void onKeyInput(KeyInputEvent event) {
        if (keyShowDifficultyBarAlways.isPressed()) {
            // Toggle the "Render Difficulty Meter Always" config option.
            Config.Client.Difficulty.renderMeterAlways = !Config.Client.Difficulty.renderMeterAlways;
            Config.INSTANCE.save();
        } else if (keyShowDifficultyBar.isPressed()) {
            // Briefly show the difficulty meter.
            DifficultyDisplayHandler.INSTANCE.showBar();
        }
    }

}
