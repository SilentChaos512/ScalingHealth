package net.silentchaos512.scalinghealth.event;

import java.util.Map;

import gnu.trove.map.hash.THashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.silentchaos512.lib.config.ConfigMultiValueLineParser;
import net.silentchaos512.lib.util.LogHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.api.ScalingHealthAPI;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;

public class DamageScaling {

  static final String[] SOURCES_DEFAULT = { "inFire", "lightningBolt", "onFire", "lava", "hotFloor", "inWall", "cramming", "drown", "starve", "cactus", "fall", "flyIntoWall", "outOfWorld", "generic",
      "magic", "wither", "anvil", "fallingBlock", "dragonBreath", "fireworks" };
  static final String SOURCES_COMMENT = "Set damage scaling by damage source. All vanilla sources should be included, but set to no scaling. Mod sources can be added too, you'll just need the damage"
      + " type string. The number represents how steeply the damage scales. 0 means no scaling (vanilla), 1 means it will be proportional to difficulty/max health (whichever you set). The scaling"
      + " number can be anything, although I recommend a non-negative number.";

  public static DamageScaling instance = new DamageScaling();

  private float genericScale;
  private float difficultyWeight;
  private Mode scaleMode;
  private Map<String, Float> scalingMap = new THashMap<>();

  @SubscribeEvent
  public void onPlayerHurt(LivingHurtEvent event) {

    if (!(event.getEntity() instanceof EntityPlayer))
      return;

    EntityPlayer player = (EntityPlayer) event.getEntity();
    DamageSource source = event.getSource();

    // Shouldn't happen but... I've seen stranger things.
    if (source == null || source.getDamageType() == null)
      return;

    // Get scaling factor from map, if it exists. Otherwise, use the generic scale.
    float scale = 0f;
    if (scalingMap.containsKey(source.getDamageType()))
      scale = scalingMap.get(source.getDamageType());
    else
      scale = genericScale;

    // Get the amount of the damage to affect. Can be many times the base value.
    float affectedAmount = 0f;
    switch (scaleMode) {
      case AREA_DIFFICULTY:
        affectedAmount = (float) ScalingHealthAPI.getAreaDifficulty(player.world, player.getPosition());
        affectedAmount *= difficultyWeight;
        break;
      case MAX_HEALTH:
        int baseHealth = ConfigScalingHealth.PLAYER_STARTING_HEALTH;
        affectedAmount = (player.getMaxHealth() - baseHealth) / baseHealth;
        break;
      case PLAYER_DIFFICULTY:
        affectedAmount = (float) ScalingHealthAPI.getPlayerDifficulty(player);
        affectedAmount *= difficultyWeight;
        break;
    }

    // Calculate damage to add to the original.
    float original = event.getAmount();
    float change = scale * affectedAmount * original;
    event.setAmount(event.getAmount() + change);

    LogHelper log = ScalingHealth.logHelper;
    if (ConfigScalingHealth.DEBUG_MODE && ConfigScalingHealth.DEBUG_LOG_PLAYER_DAMAGE) {
      String str = log.lineFromList("Player damage: " + "type=" + source.damageType, "scale=" + scale, "affected=" + affectedAmount, "change=" + change, "original=" + original,
          "new=" + event.getAmount());
      log.info(str);
    }
  }

  public void loadConfig(Configuration config) {

    final String category = ConfigScalingHealth.CAT_PLAYER_DAMAGE;

    genericScale = config.getFloat("Generic Scale", category, 0f, -Float.MAX_VALUE, Float.MAX_VALUE,
        "If the damage source is not in the \"Scale By Source\" list, this value is used instead.");
    difficultyWeight = config.getFloat("Difficulty Weight", category, 0.04f, 0f, 1000f,
        "How much each point of difficulty affects damage scaling. With the default value of 0.04 (1/25th) and max difficulty of 250, that's up to a 10x multiplier on added damage. So player's would"
            + " take 11x damage at max difficulty, if the source scale is set to 1.0.");
    scaleMode = Mode.loadConfig(config);

    // The parser is used to extract multiple values of different types from a single string. Parsing returns an Object
    // array if successful, or null if anything goes wrong. The parser also handles error logging.
    ConfigMultiValueLineParser parser = new ConfigMultiValueLineParser("Scale By Source", ScalingHealth.logHelper, "\\s", String.class, Float.class);
    scalingMap.clear();

    // Construct a default values array. Just SOURCES_DEFAULT with 0.0 appended to each element.
    String[] defaults = new String[SOURCES_DEFAULT.length];
    for (int i = 0; i < defaults.length; ++i) {
      defaults[i] = SOURCES_DEFAULT[i] + " 0.0";
    }

    for (String str : config.getStringList("Scale By Source", category, defaults, SOURCES_COMMENT)) {
      Object[] values = parser.parse(str);
      // If not null, the values are guaranteed to be the correct types.
      if (values != null)
        scalingMap.put((String) values[0], (Float) values[1]);
    }
  }

  public static enum Mode {

    MAX_HEALTH, PLAYER_DIFFICULTY, AREA_DIFFICULTY;

    public static Mode loadConfig(Configuration config) {

      String[] validValues = new String[values().length];
      for (int i = 0; i < validValues.length; ++i)
        validValues[i] = values()[i].name();

      String str = config.getString("Scaling Mode", ConfigScalingHealth.CAT_PLAYER_DAMAGE, MAX_HEALTH.name(),
          "Set what value we scale against. MAX_HEALTH scales to player's max health MINUS starting health. Defaults to MAX_HEALTH if an invalid string is entered.",
          validValues);

      for (Mode mode : values())
        if (mode.name().equals(str))
          return mode;
      return Mode.MAX_HEALTH;
    }
  }
}
