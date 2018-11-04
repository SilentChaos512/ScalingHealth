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

package net.silentchaos512.scalinghealth.utils;

import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Matches players by name.
 *
 * @deprecated Will move to Silent Lib in 1.13
 */
@Deprecated
public class PlayerMatchList {
    private final List<String> list = new ArrayList<>();

    public void add(String name) {
        list.add(name);
    }

    public void clear() {
        list.clear();
    }

    public boolean contains(EntityPlayer player) {
        if (player == null)
            return false;

        for (String name : list)
            if (name.equalsIgnoreCase(player.getName()))
                return true;
        return false;
    }
}
