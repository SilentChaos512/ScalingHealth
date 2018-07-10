package net.silentchaos512.scalinghealth.lib.module;

import java.util.Calendar;

import net.minecraftforge.common.config.Configuration;
import net.silentchaos512.scalinghealth.config.Config;

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
