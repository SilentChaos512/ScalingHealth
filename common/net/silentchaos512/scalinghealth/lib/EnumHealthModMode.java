package net.silentchaos512.scalinghealth.lib;

import net.minecraftforge.common.config.Configuration;
import net.silentchaos512.scalinghealth.config.Config;

public enum EnumHealthModMode {

  ADD(0), MULTI(1), MULTI_HALF(1), MULTI_QUARTER(1);

  public final int op;

  private EnumHealthModMode(int op) {

    this.op = op;
  }

  public static EnumHealthModMode loadFromConfig(Configuration c, EnumHealthModMode defaultValue) {

    String[] validValues = new String[values().length];
    for (int i = 0; i < values().length; ++i)
      validValues[i] = values()[i].name();

    //@formatter:off
    String str = c.getString("Scaling Mode", Config.CAT_MOB_HEALTH,
        defaultValue.name(),
        "Describes how extra mob health is applied. This will not change the health of mobs that already exist!\n"
        + "  ADD - Adds a value based on difficulty to the mob's health, ignoring the mob's default health.\n"
        + "  MULTI - Multiplies the mob's health instead of adding a flat value. For example, endermen\n"
        + "    will always have around twice the health of zombies with this option.\n"
        + "  MULTI_HALF - Multiplies the mob's health, but the value is reduced for higher-health mobs.\n"
        + "  MULTI_QUARTER - Same as MULTI_HALF, but the scaling factor is even less.",
        validValues);
    //@formatter:on

    for (EnumHealthModMode mode : values())
      if (mode.name().equals(str))
        return mode;
    return defaultValue;
  }
}
