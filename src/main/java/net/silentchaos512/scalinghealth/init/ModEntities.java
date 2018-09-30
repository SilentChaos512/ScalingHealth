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

import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.client.render.entity.RenderBlightFire;
import net.silentchaos512.scalinghealth.entity.EntityBlightFire;

public class ModEntities {
    public static void init(SRegistry reg) {
        reg.registerEntity(EntityBlightFire.class, "BlightFire", 64, 20, true);
    }

    public static void registerRenderers(SRegistry reg) {
        reg.registerEntityRenderer(EntityBlightFire.class, RenderBlightFire.Factory.INSTANCE);
    }
}
