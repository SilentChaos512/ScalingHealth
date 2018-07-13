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

package net.silentchaos512.scalinghealth.api.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;

/**
 * Events for when a blight is being made. The Pre event is posted before any changes are made.
 * Canceling Pre will prevent the mob from becoming a blight, but it will still spawn as a normal
 * mob. The Post event occurs after all changes have been made. The Post event cannot be canceled,
 * but can be used to build on top of the existing blight changes.
 *
 * @author SilentChaos512
 * @since 1.2.0
 */
public class BlightSpawnEvent extends LivingSpawnEvent {

    private BlightSpawnEvent(EntityLiving entity, World world, float x, float y, float z) {
        super(entity, world, x, y, z);
    }

    public static class Pre extends BlightSpawnEvent {

        public Pre(EntityLiving entity, World world, float x, float y, float z) {
            super(entity, world, x, y, z);
        }
    }

    public static class Post extends BlightSpawnEvent {

        public Post(EntityLiving entity, World world, float x, float y, float z) {
            super(entity, world, x, y, z);
        }

        @Override
        public boolean isCancelable() {
            return false;
        }
    }
}
