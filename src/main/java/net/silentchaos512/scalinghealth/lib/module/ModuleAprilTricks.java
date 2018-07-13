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

package net.silentchaos512.scalinghealth.lib.module;

import net.minecraftforge.common.config.Configuration;
import net.silentchaos512.scalinghealth.config.Config;

import java.util.Calendar;

public class ModuleAprilTricks {

  public static ModuleAprilTricks instance = new ModuleAprilTricks();

  public Calendar today;
  private boolean rightDay = checkDate();
  boolean moduleEnabled = true;
  boolean forcedOn = false;

  private boolean checkDate() {

    today = Calendar.getInstance();
    int year = today.get(Calendar.YEAR);
    int month = today.get(Calendar.MONTH);
    int date = today.get(Calendar.DATE);

    rightDay = month == Calendar.APRIL && // April...
        date >= 1 && date <= 2;           // April Fools Day
    return rightDay;
  }

  public boolean isEnabled() {

    return moduleEnabled || forcedOn;
  }

  public boolean isRightDay() {

    return rightDay || forcedOn;
  }

  public boolean isForcedOn() {

    return forcedOn;
  }

  public void loadConfig(Configuration c) {

    String cat = Config.CAT_HOLIDAYS + c.CATEGORY_SPLITTER + "april_trickery";
    c.setCategoryComment(cat, "April Fools event options.");
    moduleEnabled = c.getBoolean("Enabled", cat, true,
        "May cause silly things to happen on certain day(s) in April.");
    forcedOn = c.getBoolean("Forced On", cat, false, "I need this in my life 24/7!");
  }
}
