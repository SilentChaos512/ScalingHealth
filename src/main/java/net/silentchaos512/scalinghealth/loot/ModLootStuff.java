/*
 * Scaling Health -- ModLootStuff
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

package net.silentchaos512.scalinghealth.loot;

import net.minecraft.world.storage.loot.properties.EntityPropertyManager;
import net.silentchaos512.scalinghealth.loot.properties.PropertyBlight;
import net.silentchaos512.scalinghealth.loot.properties.PropertyDifficulty;

public class ModLootStuff {
    public static void registerAll() {
        EntityPropertyManager.registerProperty(new PropertyBlight.Serializer());
        EntityPropertyManager.registerProperty(new PropertyDifficulty.Serializer());
    }
}
