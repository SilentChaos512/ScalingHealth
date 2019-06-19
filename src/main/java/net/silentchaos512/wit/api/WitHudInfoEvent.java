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

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

import java.util.List;

public class WitHudInfoEvent extends WorldEvent {
    public final List<String> lines;
    public final boolean isSneaking;
    public final boolean advanced;
    public final EntityPlayer player;
    public final World world;

    public WitHudInfoEvent(EntityPlayer player, boolean advanced) {

        super(player.world);
        this.player = player;
        this.world = player.world;
        this.isSneaking = player.isSneaking();
        this.advanced = advanced;
        this.lines = Lists.newArrayList();
    }
}
