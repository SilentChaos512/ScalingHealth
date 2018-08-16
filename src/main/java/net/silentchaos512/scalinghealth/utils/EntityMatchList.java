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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO: Move to Silent Lib
public class EntityMatchList {

  List<String> list = new ArrayList<>();
  boolean whitelist = false;

  public void add(String str) {

    list.add(str);
  }

  @Deprecated
  public void clear() {

    list.clear();
  }

  public boolean matches(Entity entity) {

    boolean contains = this.contains(entity);
    return this.whitelist == contains;
  }

  @Deprecated
  public boolean contains(Entity entity) {

    ResourceLocation resource = EntityList.getKey(entity);
    if (resource == null)
      return false;

    String id = resource.toString();
    String idOld = EntityList.getEntityString(entity);

    for (String entry : list)
      if (entry.equalsIgnoreCase(id) || entry.equalsIgnoreCase(idOld) || entry.equalsIgnoreCase("minecraft:" + id))
        return true;

    return false;
  }

  public void loadConfig(Configuration config, String name, String category, String[] defaults, boolean defaultWhitelist, String comment) {

    this.clear();
    Collections.addAll(list, config.getStringList(name + " List", category, defaults, comment));

    this.whitelist = config.getBoolean(name + " IsWhitelist", category, defaultWhitelist,
        "If true, the list is a whitelist. Otherwise it is a blacklist.");
  }
}
