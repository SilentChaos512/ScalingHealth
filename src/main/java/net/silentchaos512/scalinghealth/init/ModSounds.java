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
import net.minecraftforge.registries.IForgeRegistry;
import net.silentchaos512.scalinghealth.ScalingHealth;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModSounds {
    private static final Map<String, SoundEvent> SOUND_EVENTS = new LinkedHashMap<>();

    public static SoundEvent CURSED_HEART_USE;
    public static SoundEvent ENCHANTED_HEART_USE;
    public static SoundEvent HEART_CONTAINER_USE;
    public static SoundEvent PLAYER_DIED;

    private ModSounds() { }

    public static void registerAll(RegistryEvent.Register<SoundEvent> event) {
        IForgeRegistry<SoundEvent> reg = event.getRegistry();

        CURSED_HEART_USE = register(reg, "cursed_heart_use");
        ENCHANTED_HEART_USE = register(reg, "enchanted_heart_use");
        HEART_CONTAINER_USE = register(reg, "heart_container_use");
        PLAYER_DIED = register(reg, "player_died");
    }

    private static SoundEvent register(IForgeRegistry<SoundEvent> reg, String name) {
        ResourceLocation id = new ResourceLocation(ScalingHealth.MOD_ID, name);
        SoundEvent sound = new SoundEvent(id);

        sound.setRegistryName(id);
        reg.register(sound);

        SOUND_EVENTS.put(name, sound);
        return sound;
    }
}
