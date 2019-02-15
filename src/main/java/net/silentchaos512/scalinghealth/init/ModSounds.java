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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.utils.Lazy;

import java.util.Locale;

public enum ModSounds {
    CURSED_HEART_USE,
    ENCHANTED_HEART_USE,
    HEART_CONTAINER_USE,
    PLAYER_DIED;

    private final Lazy<SoundEvent> sound;

    ModSounds() {
        this.sound = Lazy.of(() -> {
            ResourceLocation id = new ResourceLocation(ScalingHealth.MOD_ID, getName());
            return new SoundEvent(id);
        });
    }

    public SoundEvent get() {
        return sound.get();
    }

    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static void registerAll(RegistryEvent.Register<SoundEvent> event) {
        // Workaround for Forge event bus bug
        if (!event.getName().equals(ForgeRegistries.SOUND_EVENTS.getRegistryName())) return;

        for (ModSounds sound : values()) {
            register(sound.getName(), sound.get());
        }
    }

    private static void register(String name, SoundEvent sound) {
        ResourceLocation id = new ResourceLocation(ScalingHealth.MOD_ID, name);
        sound.setRegistryName(id);
        ForgeRegistries.SOUND_EVENTS.register(sound);
    }
}
