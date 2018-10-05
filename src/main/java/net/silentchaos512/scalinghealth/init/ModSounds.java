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

package net.silentchaos512.scalinghealth.init;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.ScalingHealth;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModSounds {
    private static final Map<String, SoundEvent> SOUND_EVENTS = new LinkedHashMap<>();

    public static final SoundEvent CURSED_HEART_USE = create("cursed_heart_use");
    public static final SoundEvent ENCHANTED_HEART_USE = create("enchanted_heart_use");
    public static final SoundEvent HEART_CONTAINER_USE = create("heart_container_use");
    public static final SoundEvent PLAYER_DIED = create("player_died");

    private ModSounds() {
    }

    public static void registerAll(SRegistry reg) {
        SOUND_EVENTS.forEach((name, sound) -> reg.registerSoundEvent(sound, name));
    }

    private static SoundEvent create(String soundId) {
        ResourceLocation name = new ResourceLocation(ScalingHealth.MOD_ID_LOWER, soundId);
        SoundEvent sound = new SoundEvent(name);
        SOUND_EVENTS.put(soundId, sound);
        return sound;
    }
}
