/*
 * WIT
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

package net.silentchaos512.wit.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class WitEntityInfoEvent extends WitHudInfoEvent {
    public final Entity entity;
    public final EntityLivingBase entityLiving;

    public WitEntityInfoEvent(EntityPlayer player, boolean advanced, Entity entity) {
        super(player, advanced);
        this.entity = entity;
        this.entityLiving = entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;
    }
}
