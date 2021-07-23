package net.silentchaos512.scalinghealth.config;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.silentchaos512.scalinghealth.client.gui.difficulty.DifficultyMeterShow;
import net.silentchaos512.scalinghealth.client.gui.health.AbsorptionIconStyle;
import net.silentchaos512.scalinghealth.client.gui.health.HealthTextColor;
import net.silentchaos512.scalinghealth.client.gui.health.HealthTextStyle;
import net.silentchaos512.scalinghealth.client.gui.health.HeartIconStyle;
import net.silentchaos512.utils.Anchor;
import net.silentchaos512.utils.Color;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Supplier;

import static net.minecraftforge.common.ForgeConfigSpec.*;

import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class SHConfig {
    public static class Client {
        // Debug
        public final EnumValue<Anchor> debugOverlayAnchor;
        public final DoubleValue debugOverlayTextScale;

        // Health Icons
        public final EnumValue<HeartIconStyle> heartIconStyle;
        public final ColorList heartColors;
        public final BooleanValue lastHeartOutline;
        public final ConfigValue<Integer> lastHeartOutlineColor;
        public final BooleanValue heartColorLooping;
        // Heart Tanks
        public final BooleanValue heartTanks;

        // Health Text
        public final EnumValue<HealthTextStyle> healthTextStyle;
        public final DoubleValue healthTextScale;
        public final IntValue healthTextOffsetX;
        public final IntValue healthTextOffsetY;
        public final EnumValue<HealthTextColor> healthTextColorStyle;
        public final ConfigValue<Integer> healthTextFullColor;
        public final ConfigValue<Integer> healthTextEmptyColor;

        // Absorption Icons
        public final EnumValue<AbsorptionIconStyle> absorptionIconStyle;
        public final ColorList absorptionHeartColors;

        // Absorption Text
        public final EnumValue<HealthTextStyle> absorptionTextStyle;
        public final IntValue absorptionTextOffsetX;
        public final IntValue absorptionTextOffsetY;
        public final ConfigValue<Integer> absorptionTextColor;

        //Blights
        public final BooleanValue displayBlightEffect;
        // Difficulty
        public final BooleanValue warnWhenSleeping;
        // Difficulty Meter
        public final EnumValue<DifficultyMeterShow> difficultyMeterShow;
        public final DoubleValue difficultyMeterShowTime;
        public final EnumValue<Anchor> difficultyMeterAnchor;
        public final IntValue difficultyMeterOffsetX;
        public final IntValue difficultyMeterOffsetY;
        public final DoubleValue difficultyMeterTextScale;

        public Client(Builder builder) {
            debugOverlayAnchor = builder
                    .comment("Position of debug overlay")
                    .defineEnum("debug.overlay.anchor", Anchor.TOP_RIGHT);
            debugOverlayTextScale = builder
                    .comment("Overlay text size, where 1 is standard-sized text")
                    .defineInRange("debug.overlay.textScale",0.75, 0.01, Double.MAX_VALUE);

            //region Health Hearts

            heartIconStyle = builder
                    .comment("Heart style",
                            "REPLACE_ALL: All rows replaced with Scaling Health style hearts",
                            "REPLACE_AFTER_FIRST_ROW: Leave the first row vanilla style, Scaling Health style for additional rows",
                            "VANILLA: Do not change heart rendering (use this if you want another mod to handle heart rendering)")
                    .defineEnum("hearts.health.icons.style", HeartIconStyle.REPLACE_ALL);
            heartColors = new ColorList(builder, "hearts.health.icons.colors",
                    "The color of each row of hearts. If the player has more rows than colors, it starts over from the beginning.",
                    0xBF0000, // 0 red
                    0xE66000, // 25 orange-red
                    0xE69900, // 40 orange
                    0xE6D300, // 55 yellow
                    0x99E600, // 80 lime
                    0x4CE600, // 100 green
                    0x00E699, // 160 teal
                    0x00E6E6, // 180 aqua
                    0x0099E6, // 200 sky blue
                    0x0000E6, // 240 blue
                    0x9900E6, // 280 dark purple
                    0xD580FF, // 280 light purple
                    0x8C8C8C, // 0 gray
                    0xE6E6E6  // 0 white
            );
            lastHeartOutline = builder
                    .comment("The player's highest heart will get an outline around it.")
                    .define("hearts.health.icons.lastHeartOutline", true);
            lastHeartOutlineColor = builder
                    .comment("The color of the last heart outline, if enabled (see lastHeartOutline)")
                    .define("hearts.health.icons.lastHeartOutlineColor", Color.VALUE_WHITE, SHConfig::validateColor);
            heartColorLooping = builder
                    .comment("If true, heart colors will 'loop around' to the first color after going through the",
                            "entire list. Set false to have every row after the last have the same color.")
                    .define("hearts.health.icons.colorLooping",true);

            // Tanks
            heartTanks = builder
                    .comment("Enable heart tanks, the small icons above your hearts which indicate the number of filled health rows")
                    .define("hearts.health.tanks.enabled", true);

            healthTextStyle = builder
                    .comment("Style of health text")
                    .defineEnum("hearts.health.text.style", HealthTextStyle.ROWS);
            healthTextScale = builder
                    .comment("Health text scale, relative to its normal size (which varies by style)")
                    .defineInRange("hearts.health.text.scale", 1.0, 0.01, Double.MAX_VALUE);
            healthTextOffsetX = builder
                    .comment("Fine-tune text position")
                    .defineInRange("hearts.health.text.offsetX", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            healthTextOffsetY = builder
                    .comment("Fine-tune text position")
                    .defineInRange("hearts.health.text.offsetY", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

            healthTextColorStyle = builder
                    .comment("Health text color style.",
                            "TRANSITION: Gradually goes from full color to empty color as health is lost",
                            "PSYCHEDELIC: Taste the rainbow!",
                            "SOLID: Just stays at full color regardless of health")
                    .defineEnum("hearts.health.text.color.style", HealthTextColor.TRANSITION);
            healthTextFullColor = builder
                    .comment("Color when health is full or style is SOLID")
                    .define("hearts.health.text.color.full", 0x4CFF4C, SHConfig::validateColor);
            healthTextEmptyColor = builder
                    .comment("Color when health is empty and style is TRANSITION")
                    .define("hearts.health.text.color.empty", 0xFF4C4C, SHConfig::validateColor);

            //endregion

            //region Absorption Hearts

            absorptionIconStyle = builder
                    .comment("Style of absorption icons")
                    .defineEnum("hearts.absorption.icons.style", AbsorptionIconStyle.SHIELD);
            absorptionHeartColors = new ColorList(builder, "hearts.absorption.icons.colors",
                    "The color of each row of absorption hearts. If the player has more rows than colors, it starts over from the beginning.",
                    0xBF0000, // 0 red
                    0xE66000, // 25 orange-red
                    0xE69900, // 40 orange
                    0xE6D300, // 55 yellow
                    0x99E600, // 80 lime
                    0x4CE600, // 100 green
                    0x00E699, // 160 teal
                    0x00E6E6, // 180 aqua
                    0x0099E6, // 200 sky blue
                    0x0000E6, // 240 blue
                    0x9900E6, // 280 dark purple
                    0xD580FF, // 280 light purple
                    0x8C8C8C, // 0 gray
                    0xE6E6E6  // 0 white
            );

            absorptionTextStyle = builder
                    .comment("Style for absorption text. Because there is no 'max' value, the options are more limited.")
                    .defineEnum("hearts.absorption.text.style", HealthTextStyle.DISABLED, ImmutableList.of(HealthTextStyle.DISABLED, HealthTextStyle.HEALTH_ONLY, HealthTextStyle.ROWS));
            absorptionTextOffsetX = builder
                    .comment("Fine-tune text position")
                    .defineInRange("hearts.absorption.text.offsetX", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            absorptionTextOffsetY = builder
                    .comment("Fine-tune text position")
                    .defineInRange("hearts.absorption.text.offsetY", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            absorptionTextColor = builder
                    .comment("The color of the absorption text")
                    .define("hearts.absorption.text.color", Color.VALUE_WHITE, SHConfig::validateColor);

            //endregion

            //region Difficulty

            warnWhenSleeping = builder
                    .comment("Display a warning to players trying to sleep, to remind them their difficulty may change. Sleeping is still allowed.")
                    .define("difficulty.warnWhenSleeping", true);

            difficultyMeterShow = builder
                    .comment("When to show the difficulty meter.")
                    .defineEnum("difficulty.meter.show", DifficultyMeterShow.SOMETIMES);
            difficultyMeterShowTime = builder
                    .comment("Show the difficulty meter for this many seconds (only on SOMETIMES mode)")
                    .defineInRange("difficulty.meter.showDuration", 8, 0, Double.MAX_VALUE);

            difficultyMeterAnchor = builder
                    .comment("Position of the difficulty meter.")
                    .defineEnum("difficulty.meter.position.anchor", Anchor.BOTTOM_LEFT);
            difficultyMeterOffsetX = builder
                    .comment("Fine-tune the difficulty meter's position")
                    .defineInRange("difficulty.meter.position.offsetX",5, Integer.MIN_VALUE, Integer.MAX_VALUE);
            difficultyMeterOffsetY = builder
                    .comment("Fine-tune the difficulty meter's position")
                    .defineInRange("difficulty.meter.position.offsetY", -30, Integer.MIN_VALUE, Integer.MAX_VALUE);
            difficultyMeterTextScale = builder
                    .comment("Scale of text on the difficulty meter")
                    .defineInRange("difficulty.meter.text.scale", 0.6, 0, Double.MAX_VALUE);
            displayBlightEffect = builder
                    .comment("whether the purple flame should render on blights or not.",
                            "This does not change whether the mob is a blight, only hides the effect.")
                    .define("blights.render", true);
            //endregion
        }

        public void reload() {
            this.absorptionHeartColors.recalculate();
            this.heartColors.recalculate();
        }
    }

    public static class Server {
        public final BooleanValue debugMaster;
        public final Supplier<Boolean> debugShowOverlay;
        public final Supplier<Boolean> debugLogEntitySpawns;
        public final Supplier<Boolean> debugLogScaledDamage;
        public final Supplier<Boolean> debugMobPotionEffects;

        public final BooleanValue crystalsAddHealth;
        public final BooleanValue xpAddHealth;
        public final BooleanValue crystalsRegenHealth;
        public final BooleanValue crystalsAddPetHealth;

        public final BooleanValue crystalsAddDamage;

        public final BooleanValue hpCrystalsOreGen;
        public final BooleanValue powerCrystalsOreGen;

        public final BooleanValue mobDamageIncrease;
        public final BooleanValue mobHpIncrease;

        public final BooleanValue playerDamageScaling;
        public final BooleanValue mobDamageScaling;

        public final BooleanValue enableDifficulty;
        public final BooleanValue enableBlights;

        public Server(Builder builder) {
            builder.comment("All SH features can be disabled here. False to disable.")
                    .push("features");

            crystalsAddHealth = builder
                    .comment("Enable player bonus hp by crystals.")
                    .define("crystalsAddHealth", true);

            xpAddHealth = builder
                    .comment("Enable player bonus hp by xp.")
                    .define("xpAddHealth", true);

            crystalsRegenHealth = builder
                    .comment("Enable player regen hp by crystals.")
                    .define("crystalsRegenHealth", true);

            crystalsAddPetHealth = builder
                    .comment("Enable pet add hp by crystals.")
                    .define("crystalsAddPetHealth", true);

            crystalsAddDamage = builder
                    .comment("Enable player add damage by crystals.")
                    .define("crystalsAddDamage", true);

            hpCrystalsOreGen = builder
                    .comment("Enable ore gen of health crystals. Still drops as loot.")
                    .define("hpCrystalsOreGen", true);

            powerCrystalsOreGen = builder
                    .comment("Enable ore gen of power crystals. Still drops as loot.")
                    .define("powerCrystalsOreGen", true);

            mobHpIncrease = builder
                    .comment("Mobs will gain bonus health with difficulty.")
                    .define("mobHpIncrease", true);

            mobDamageIncrease = builder
                    .comment("Mobs will gain bonus damage with difficulty.")
                    .define("mobDamageIncrease", true);

            playerDamageScaling = builder
                    .comment("Enable player damage scaling.")
                    .define("playerDamageScaling", true);

            mobDamageScaling = builder
                    .comment("Enable mob damage scaling.")
                    .define("mobDamageScaling", true);

            enableDifficulty = builder
                    .comment("Enable difficulty system. If disabled, everything will have 0 difficulty.")
                    .define("enableDifficulty", true);

            enableBlights = builder
                    .comment("Enable blights. If disabled, no blights will spawn.")
                    .define("enableBlights", true);

            builder.pop().comment(
                    "Debug settings are intended for tuning configs or diagnosing issues.",
                    "They may decrease performance and should be disabled for normal play."
            ).push("debug");

            debugMaster = builder
                    .comment("Must be true for other debug settings to apply")
                    .define("masterSwitch", false);
            debugShowOverlay = withMasterCheck(builder
                    .comment("Show some text in-game about player health, difficulty, and maybe other things.")
                    .define("showOverlay", true)
            );
            debugLogEntitySpawns = withMasterCheck(builder
                    .comment("Log details of entity spawns, including effects of difficulty.",
                            "This creates a lot of log spam, and will likely lag the game.")
                    .define("logEntitySpawns", false)
            );
            debugMobPotionEffects = withMasterCheck(builder
                    .comment("Logs details of potions effects added to mobs.")
                    .define("logApplyingMobPotions", false)
            );
            debugLogScaledDamage = withMasterCheck(builder
                    .comment("Log details of scaled damage, useful for fine-tuning damage scaling.",
                            "May create a fair amount of log spam, but shouldn't slow down the game too much.")
                    .define("logDamageScaling", false)
            );
        }

        private Supplier<Boolean> withMasterCheck(BooleanValue option) {
            return () -> debugMaster.get() && option.get();
        }
    }

    private static final ForgeConfigSpec CLIENT_SPEC;
    private static final ForgeConfigSpec SERVER_SPEC;

    public static final Client CLIENT;
    public static final Server SERVER;

    static {
        Pair<Client, ForgeConfigSpec> pCli = new Builder().configure(Client::new);
        CLIENT_SPEC = pCli.getRight();
        CLIENT = pCli.getLeft();

        Pair<Server, ForgeConfigSpec> pSev = new Builder().configure(Server::new);
        SERVER_SPEC = pSev.getRight();
        SERVER = pSev.getLeft();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    }

    private static boolean validateColor(Object o) {
        return o instanceof Integer && Color.validate(o.toString());
    }
}
