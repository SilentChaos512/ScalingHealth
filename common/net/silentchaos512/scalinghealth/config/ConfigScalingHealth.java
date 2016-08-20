package net.silentchaos512.scalinghealth.config;

import java.io.File;
import java.util.List;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.lib.EnumAreaDifficultyMode;
import net.silentchaos512.scalinghealth.lib.EnumHealthModMode;

public class ConfigScalingHealth {

  public static boolean DEBUG_MODE = false;

  // Client
  public static boolean CHANGE_HEART_RENDERING = true;  // TODO: Change?
  public static boolean RENDER_DIFFICULTY_METER = true; // TODO: Change?

  // Player Health
  public static int PLAYER_STARTING_HEALTH = 20;
  public static int PLAYER_HEALTH_MAX = 0;
  public static boolean LOSE_HEALTH_ON_DEATH = false;
  // Regen
  public static boolean ENABLE_BONUS_HEALTH_REGEN = true;
  public static int BONUS_HEALTH_REGEN_MIN_FOOD = 10;
  public static int BONUS_HEALTH_REGEN_MAX_FOOD = 20;
  public static int BONUS_HEALTH_REGEN_INITIAL_DELAY = 400;
  public static int BONUS_HEALTH_REGEN_DELAY = 100;

  // Mob Health
  public static boolean ALLOW_PEACEFUL_EXTRA_HEALTH = true;
  public static boolean ALLOW_HOSTILE_EXTRA_HEALTH = true;
  public static float DIFFICULTY_GENERIC_HEALTH_MULTIPLIER = 0.5F;
  public static float DIFFICULTY_PEACEFUL_HEALTH_MULTIPLIER = 0.25F;
  public static EnumHealthModMode MOB_HEALTH_SCALING_MODE = EnumHealthModMode.MULTI_HALF;
  // Blights
  public static float BLIGHT_CHANCE_MULTIPLIER = 0.0625F;
  public static int BLIGHT_AMP_SPEED = 8;
  public static int BLIGHT_AMP_STRENGTH = 2;

  // Items
  public static float HEART_DROP_CHANCE = 0.01F;
  public static int HEARTS_DROPPED_BY_BOSS_MIN = 3;
  public static int HEARTS_DROPPED_BY_BOSS_MAX = 6;

  // Difficulty
  public static float DIFFICULTY_MAX = 250;
  public static float DIFFICULTY_DEFAULT = 0;
  public static int HOURS_TO_MAX_DIFFICULTY = 60; // Not actually loaded, just to make calc below clear.
  public static float DIFFICULTY_PER_SECOND = DIFFICULTY_MAX / (HOURS_TO_MAX_DIFFICULTY * 3600);
  public static float DIFFICULTY_PER_BLOCK = DIFFICULTY_MAX / 100000;
  public static float DIFFICULTY_IDLE_MULTI = 0.7f;
  public static float DIFFICULTY_GROUP_AREA_BONUS = 0.05f;
  public static int DIFFICULTY_SEARCH_RADIUS = 160;
  public static EnumAreaDifficultyMode AREA_DIFFICULTY_MODE = EnumAreaDifficultyMode.WEIGHTED_AVERAGE;

  // Network
  public static int PACKET_DELAY = 20;

  static final String split = Configuration.CATEGORY_SPLITTER;
  public static final String CAT_MAIN = "main";
  public static final String CAT_CLIENT = CAT_MAIN + split + "client";
  public static final String CAT_PLAYER = CAT_MAIN + split + "player";
  public static final String CAT_PLAYER_HEALTH = CAT_PLAYER + split + "health";
  public static final String CAT_PLAYER_REGEN = CAT_PLAYER + split + "regen";
  public static final String CAT_MOB = CAT_MAIN + split + "mob";
  public static final String CAT_MOB_HEALTH = CAT_MOB + split + "health";
  public static final String CAT_MOB_BLIGHT = CAT_MOB + split + "blights";
  public static final String CAT_ITEMS = CAT_MAIN + split + "items";
  public static final String CAT_DIFFICULTY = CAT_MAIN + split + "difficulty";
  public static final String CAT_NETWORK = CAT_MAIN + split + "network";

  private static Configuration c;

  public static void init(File file) {

    String path = file.getPath().replaceFirst("\\.cfg$", "/main.cfg");
    c = new Configuration(new File(path));
    load();
  }

  public static void load() {

    try {
      //@formatter:off

      DEBUG_MODE = c.getBoolean("Debug Mode", CAT_MAIN,
          DEBUG_MODE,
          "Draws random stuffs on the screen! And maybe does some other things.");

      // Client
      CHANGE_HEART_RENDERING = c.getBoolean("Custom Heart Rendering", CAT_CLIENT,
          CHANGE_HEART_RENDERING,
          "Replace vanilla heart rendering.");

      // Players
      // Health
      PLAYER_STARTING_HEALTH = c.getInt("Starting Health", CAT_PLAYER_HEALTH,
          PLAYER_STARTING_HEALTH, 2, Integer.MAX_VALUE,
          "The amount of health (in half hearts) the player starts with.");
      PLAYER_HEALTH_MAX = c.getInt("Max Health", CAT_PLAYER_HEALTH,
          PLAYER_HEALTH_MAX, 0, Integer.MAX_VALUE,
          "The maximum amount of health (in half hearts) the player can reach. Zero means unlimited.");
      LOSE_HEALTH_ON_DEATH = c.getBoolean("Lose Health On Death", CAT_PLAYER_HEALTH,
          LOSE_HEALTH_ON_DEATH,
          "If true, the player's health will be reset to the default starting health on death.");
      // Regen
      ENABLE_BONUS_HEALTH_REGEN = c.getBoolean("Enable Bonus Regen", CAT_PLAYER_REGEN,
          ENABLE_BONUS_HEALTH_REGEN,
          "Bonus health regen will be enabled. Vanilla regen is not changed in any way, this just adds extra healing!");
      BONUS_HEALTH_REGEN_MIN_FOOD = c.getInt("Food Min", CAT_PLAYER_REGEN,
          BONUS_HEALTH_REGEN_MIN_FOOD, 0, 20,
          "The minimum food level at which bonus regen will be active.");
      BONUS_HEALTH_REGEN_MAX_FOOD = c.getInt("Food Max", CAT_PLAYER_REGEN,
          BONUS_HEALTH_REGEN_MAX_FOOD, 0, 20,
          "The maximum food level at which bonus regen will be active.");
      BONUS_HEALTH_REGEN_INITIAL_DELAY = c.getInt("Delay (Initial)", CAT_PLAYER_REGEN,
          BONUS_HEALTH_REGEN_INITIAL_DELAY, 0, Integer.MAX_VALUE,
          "The number of ticks after being hurt before bonus regen activates.");
      BONUS_HEALTH_REGEN_DELAY = c.getInt("Delay", CAT_PLAYER_REGEN,
          BONUS_HEALTH_REGEN_DELAY, 0, Integer.MAX_VALUE,
          "The number of ticks between each bonus regen tick (a half heart being healed).");

      // Mobs
      // Health
      ALLOW_PEACEFUL_EXTRA_HEALTH = c.getBoolean("Allow Peaceful Extra Health", CAT_MOB_HEALTH,
          ALLOW_PEACEFUL_EXTRA_HEALTH,
          "Allow peaceful mobs (such as animals) to spawn with extra health based on difficulty.");
      ALLOW_HOSTILE_EXTRA_HEALTH = c.getBoolean("Allow Hostile Extra Health", CAT_MOB_HEALTH,
          ALLOW_HOSTILE_EXTRA_HEALTH,
          "Allow hostile mobs (monsters) to spawn with extra health based on difficulty.");
      DIFFICULTY_GENERIC_HEALTH_MULTIPLIER = c.getFloat("Base Health Modifier", CAT_MOB_HEALTH,
          DIFFICULTY_GENERIC_HEALTH_MULTIPLIER, 0f, Float.MAX_VALUE,
          "The minimum extra health a mob will have per point of difficulty. For example, at difficulty 30, "
              + "a mob that normally has 20 health would have at least 50 health.");
      DIFFICULTY_PEACEFUL_HEALTH_MULTIPLIER = c.getFloat("Base Health Modifier Peaceful", CAT_MOB_HEALTH,
          DIFFICULTY_PEACEFUL_HEALTH_MULTIPLIER, 0f, Float.MAX_VALUE,
          "The minimum extra health a peaceful will have per point of difficulty. Same as "
              + "\"Base Health Modifier\", but for peaceful mobs!");
      MOB_HEALTH_SCALING_MODE = EnumHealthModMode.loadFromConfig(c, MOB_HEALTH_SCALING_MODE);
      // Blights
      BLIGHT_CHANCE_MULTIPLIER = c.getFloat("Blight Chance Multiplier", CAT_MOB_BLIGHT,
          BLIGHT_CHANCE_MULTIPLIER, 0f, Float.MAX_VALUE,
          "Determines the chance of a mob spawning as a blight. Formula is "
              + "blightChanceMulti * currentDifficulty / maxDifficulty");
      BLIGHT_AMP_SPEED = c.getInt("Amplifier Speed", CAT_MOB_BLIGHT,
          BLIGHT_AMP_SPEED, 0, 99,
          "The amplifier level on the speed potion effect applied to blights.");
      BLIGHT_AMP_STRENGTH = c.getInt("Amplifier Strength", CAT_MOB_BLIGHT,
          BLIGHT_AMP_STRENGTH, 0, 99,
          "The amplifier level on the strength potion effect applied to blights.");

      // Items
      HEART_DROP_CHANCE = c.getFloat("Heart Drop Chance", CAT_ITEMS,
          HEART_DROP_CHANCE, 0f, 1f,
          "The chance of any mob dropping a heart canister when killed.");
      HEARTS_DROPPED_BY_BOSS_MIN = c.getInt("Hearts Dropped by Boss Min", CAT_ITEMS,
          HEARTS_DROPPED_BY_BOSS_MIN, 0, 64,
          "The minimum number of heart canisters that a boss will drop when killed.");
      HEARTS_DROPPED_BY_BOSS_MAX = c.getInt("Hearts Dropped by Boss Max", CAT_ITEMS,
          HEARTS_DROPPED_BY_BOSS_MAX, 0, 64,
          "The maximum number of heart canisters that a boss will drop when killed.");
      if (HEARTS_DROPPED_BY_BOSS_MAX < HEARTS_DROPPED_BY_BOSS_MIN)
        HEARTS_DROPPED_BY_BOSS_MAX = HEARTS_DROPPED_BY_BOSS_MIN;

      // Difficulty
      DIFFICULTY_MAX = c.getFloat("Max Value", CAT_DIFFICULTY,
          DIFFICULTY_MAX, 0f, Float.MAX_VALUE,
          "The maximum difficult level that can be reached.");
      DIFFICULTY_DEFAULT = c.getFloat("Starting Value", CAT_DIFFICULTY,
          DIFFICULTY_DEFAULT, 0f, Float.MAX_VALUE,
          "The starting difficulty level for new worlds.");
      DIFFICULTY_PER_SECOND = c.getFloat("Increase Per Second", CAT_DIFFICULTY,
          DIFFICULTY_PER_SECOND, 0f, Float.MAX_VALUE,
          "The amount of difficulty added each second. In Difficult Life, the option was named per tick, "
          + "but was actually applied each second.");
      DIFFICULTY_PER_BLOCK = c.getFloat("Difficulty Per Block", CAT_DIFFICULTY,
          DIFFICULTY_PER_BLOCK, 0, Float.MAX_VALUE,
          "The amount of difficulty added per unit distance from the origin/spanw, assuming \"Area Mode\" "
          + "is set to a distance-based option.");
      DIFFICULTY_IDLE_MULTI = c.getFloat("Idle Multiplier", CAT_DIFFICULTY,
          DIFFICULTY_IDLE_MULTI, 0f, Float.MAX_VALUE,
          "Difficulty added per second is multiplied by this if the player is not moving.");
      DIFFICULTY_GROUP_AREA_BONUS = c.getFloat("Group Area Bonus", CAT_DIFFICULTY,
          DIFFICULTY_GROUP_AREA_BONUS, 0f, Float.MAX_VALUE,
          "Adds this much extra difficulty per additional player in the area. So, area difficulty will\n"
          + "be multiplied by: 1 + group_bonus * (players_in_area - 1)");
      DIFFICULTY_SEARCH_RADIUS = c.getInt("Search Radius", CAT_DIFFICULTY,
          DIFFICULTY_SEARCH_RADIUS, 0, Short.MAX_VALUE,
          "The distance from a newly spawned mob to search for players to determine its difficulty "
          + "level. Set to 0 for unlimited range.");
      AREA_DIFFICULTY_MODE = EnumAreaDifficultyMode.loadFromConfig(c, AREA_DIFFICULTY_MODE);

      // Network
      PACKET_DELAY = c.getInt("Packet Delay", CAT_NETWORK,
          PACKET_DELAY, 1, 1200,
          "The number of ticks between update packets. Smaller numbers mean more packets (and more "
          + "bandwidth and processing power used), but also less client-server desync.");

      //@formatter:on
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
