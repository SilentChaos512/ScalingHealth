package net.silentchaos512.scalinghealth.config;

import java.io.File;
import java.util.List;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.silentchaos512.scalinghealth.ScalingHealth;

public class ConfigScalingHealth {

  public static boolean ALLOW_PEACEFUL_EXTRA_HEALTH = true;
  public static boolean ALLOW_HOSTILE_EXTRA_HEALTH = true;
  public static int PLAYER_STARTING_HEALTH = 20;
  public static int PLAYER_HEALTH_MAX = 0;
  public static double DIFFICULTY_MAX = 250;
  public static double DIFFICULTY_DEFAULT = 0;
  public static int HOURS_TO_MAX_DIFFICULTY = 48;
  public static double DIFFICULTY_PER_TICK = DIFFICULTY_MAX / (HOURS_TO_MAX_DIFFICULTY * 72000);
  public static float DIFFICULTY_GENERIC_HEALTH_MULTIPLIER = 0.5F;
  public static float DIFFICULTY_PEACEFUL_HEALTH_MULTIPLIER = 0.25F;
  public static boolean ENABLE_BONUS_HEALTH_REGEN = true;
  public static float BLIGHT_CHANCE_MULTIPLIER = 0.0625F;
  public static float HEART_DROP_CHANCE = 0.01F;
  public static boolean CHANGE_HEART_RENDERING = false; // TODO: Change?
  public static boolean RENDER_DIFFICULTY_METER = true; // TODO: Change?
  public static int PACKET_DELAY = 20;
  public static boolean LOSE_HEALTH_ON_DEATH = true;

  public static int HEARTS_DROPPED_BY_BOSS_MIN = 3;
  public static int HEARTS_DROPPED_BY_BOSS_MAX = 6;

  public static int BLIGHT_AMP_SPEED = 8;
  public static int BLIGHT_AMP_STRENGTH = 2;

  static final String splitter = Configuration.CATEGORY_SPLITTER;
  public static final String CAT_MAIN = "main";

  private static Configuration c;

  public static void init(File file) {

    c = new Configuration(file);
    load();
  }

  public static void load() {

    try {
      // TODO
    } catch (Exception ex) {
      ScalingHealth.logHelper.severe("Could not load configuration file!");
    }
  }

  public static void save() {

    if (c.hasChanged())
      c.save();
  }

  public static ConfigCategory getCategory(String str) {

    return c.getCategory(str);
  }

  public static Configuration getConfiguration() {

    return c;
  }

  public static List<IConfigElement> getConfigElements() {

    return new ConfigElement(getCategory(CAT_MAIN)).getChildElements();
  }
}
