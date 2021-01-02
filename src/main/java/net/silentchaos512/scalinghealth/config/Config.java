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

package net.silentchaos512.scalinghealth.config;

import net.minecraftforge.fml.loading.FMLPaths;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.client.gui.difficulty.DifficultyMeterShow;
import net.silentchaos512.scalinghealth.client.gui.health.AbsorptionIconStyle;
import net.silentchaos512.scalinghealth.client.gui.health.HealthTextColor;
import net.silentchaos512.scalinghealth.client.gui.health.HealthTextStyle;
import net.silentchaos512.scalinghealth.client.gui.health.HeartIconStyle;
import net.silentchaos512.utils.Anchor;
import net.silentchaos512.utils.Color;
import net.silentchaos512.utils.config.*;

import java.io.File;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.function.Supplier;

public final class Config {
    // config/scaling-health
    private static final Path SH_CONFIG_DIR = resolve(FMLPaths.CONFIGDIR.get(), "scaling-health");

    private static final ConfigSpecWrapper WRAPPER_GAME = ConfigSpecWrapper.create(resolve(SH_CONFIG_DIR, "game_settings.toml"));
    private static final ConfigSpecWrapper WRAPPER_CLIENT = ConfigSpecWrapper.create(resolve(SH_CONFIG_DIR, "client.toml"));
    private static final ConfigSpecWrapper WRAPPER_COMMON = ConfigSpecWrapper.create(resolve(SH_CONFIG_DIR, "common.toml"));

    public static final Client CLIENT = new Client(WRAPPER_CLIENT);
    public static final Common COMMON = new Common(WRAPPER_COMMON);
    public static final GameConfig GENERAL = new GameConfig(WRAPPER_GAME);

    public static class Client {
        // Debug
        public final EnumValue<Anchor> debugOverlayAnchor;
        public final DoubleValue debugOverlayTextScale;

        // Health Icons
        public final EnumValue<HeartIconStyle> heartIconStyle;
        public final ColorList heartColors;
        public final BooleanValue lastHeartOutline;
        public final IntValue lastHeartOutlineColor;
        public final BooleanValue heartColorLooping;
        // Heart Tanks
        public final BooleanValue heartTanks;

        // Health Text
        public final EnumValue<HealthTextStyle> healthTextStyle;
        public final DoubleValue healthTextScale;
        public final IntValue healthTextOffsetX;
        public final IntValue healthTextOffsetY;
        public final EnumValue<HealthTextColor> healthTextColorStyle;
        public final IntValue healthTextFullColor;
        public final IntValue healthTextEmptyColor;

        // Absorption Icons
        public final EnumValue<AbsorptionIconStyle> absorptionIconStyle;
        public final ColorList absorptionHeartColors;

        // Absorption Text
        public final EnumValue<HealthTextStyle> absorptionTextStyle;
        public final IntValue absorptionTextOffsetX;
        public final IntValue absorptionTextOffsetY;
        public final IntValue absorptionTextColor;

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

        Client(ConfigSpecWrapper wrapper) {
            debugOverlayAnchor = wrapper
                    .builder("debug.overlay.anchor")
                    .comment("Position of debug overlay", EnumValue.allValuesComment(Anchor.class))
                    .defineEnum(Anchor.TOP_RIGHT);
            debugOverlayTextScale = wrapper
                    .builder("debug.overlay.textScale")
                    .comment("Overlay text size, where 1 is standard-sized text")
                    .defineInRange(0.75, 0.01, Double.MAX_VALUE);

            //region Health Hearts

            wrapper.comment("hearts.health.icons", "Settings for heart rows");

            heartIconStyle = wrapper
                    .builder("hearts.health.icons.style")
                    .comment("Heart style",
                            "REPLACE_ALL: All rows replaced with Scaling Health style hearts",
                            "REPLACE_AFTER_FIRST_ROW: Leave the first row vanilla style, Scaling Health style for additional rows",
                            "VANILLA: Do not change heart rendering (use this if you want another mod to handle heart rendering)")
                    .defineEnum(HeartIconStyle.REPLACE_ALL);
            heartColors = new ColorList(wrapper, "hearts.health.icons.colors",
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
            lastHeartOutline = wrapper
                    .builder("hearts.health.icons.lastHeartOutline")
                    .comment("The player's highest heart will get an outline around it.")
                    .define(true);
            lastHeartOutlineColor = wrapper
                    .builder("hearts.health.icons.lastHeartOutlineColor")
                    .comment("The color of the last heart outline, if enabled (see lastHeartOutline)")
                    .defineColorInt(Color.VALUE_WHITE);
            heartColorLooping = wrapper
                    .builder("hearts.health.icons.colorLooping")
                    .comment("If true, heart colors will 'loop around' to the first color after going through the",
                            "entire list. Set false to have every row after the last have the same color.")
                    .define(true);

            // Tanks
            heartTanks = wrapper
                    .builder("hearts.health.tanks.enabled")
                    .comment("Enable heart tanks, the small icons above your hearts which indicate the number of filled health rows")
                    .define(true);

            wrapper.comment("hearts.health.text", "Settings for the text displayed next to the heart rows");

            healthTextStyle = wrapper
                    .builder("hearts.health.text.style")
                    .comment("Style of health text", EnumValue.allValuesComment(HealthTextStyle.class))
                    .defineEnum(HealthTextStyle.ROWS);
            healthTextScale = wrapper
                    .builder("hearts.health.text.scale")
                    .comment("Health text scale, relative to its normal size (which varies by style)")
                    .defineInRange(1.0, 0.01, Double.MAX_VALUE);
            healthTextOffsetX = wrapper
                    .builder("hearts.health.text.offsetX")
                    .comment("Fine-tune text position")
                    .defineInRange(0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            healthTextOffsetY = wrapper
                    .builder("hearts.health.text.offsetY")
                    .comment("Fine-tune text position")
                    .defineInRange(0, Integer.MIN_VALUE, Integer.MAX_VALUE);

            healthTextColorStyle = wrapper
                    .builder("hearts.health.text.color.style")
                    .comment("Health text color style.",
                            "TRANSITION: Gradually goes from full color to empty color as health is lost",
                            "PSYCHEDELIC: Taste the rainbow!",
                            "SOLID: Just stays at full color regardless of health")
                    .defineEnum(HealthTextColor.TRANSITION);
            healthTextFullColor = wrapper
                    .builder("hearts.health.text.color.full")
                    .comment("Color when health is full or style is SOLID")
                    .defineColorInt(0x4CFF4C);
            healthTextEmptyColor = wrapper
                    .builder("hearts.health.text.color.empty")
                    .comment("Color when health is empty and style is TRANSITION")
                    .defineColorInt(0xFF4C4C);

            //endregion

            //region Absorption Hearts

            absorptionIconStyle = wrapper
                    .builder("hearts.absorption.icons.style")
                    .comment("Style of absorption icons", EnumValue.allValuesComment(AbsorptionIconStyle.class))
                    .defineEnum(AbsorptionIconStyle.SHIELD);
            absorptionHeartColors = new ColorList(wrapper, "hearts.absorption.icons.colors",
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

            EnumSet<HealthTextStyle> absorptionTextStyleValidValues = EnumSet.of(
                    HealthTextStyle.DISABLED, HealthTextStyle.HEALTH_ONLY, HealthTextStyle.ROWS);
            absorptionTextStyle = wrapper
                    .builder("hearts.absorption.text.style")
                    .comment("Style for absorption text. Because there is no 'max' value, the options are more limited.",
                            EnumValue.validValuesComment(absorptionTextStyleValidValues))
                    .defineEnum(HealthTextStyle.DISABLED, absorptionTextStyleValidValues);
            absorptionTextOffsetX = wrapper
                    .builder("hearts.absorption.text.offsetX")
                    .comment("Fine-tune text position")
                    .defineInRange(0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            absorptionTextOffsetY = wrapper
                    .builder("hearts.absorption.text.offsetY")
                    .comment("Fine-tune text position")
                    .defineInRange(0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            absorptionTextColor = wrapper
                    .builder("hearts.absorption.text.color")
                    .comment("The color of the absorption text")
                    .defineColorInt(Color.VALUE_WHITE);

            //endregion

            //region Difficulty

            warnWhenSleeping = wrapper
                    .builder("difficulty.warnWhenSleeping")
                    .comment("Display a warning to players trying to sleep, to remind them their difficulty may change. Sleeping is still allowed.")
                    .define(true);

            difficultyMeterShow = wrapper
                    .builder("difficulty.meter.show")
                    .comment("When to show the difficulty meter.",
                            " SOMETIMES will show the meter for a few seconds every so often.",
                            "KEYPRESS will make the meter keybinded",
                            "ALWAYS and NEVER should be obvious enough.")
                    .defineEnum(DifficultyMeterShow.SOMETIMES);
            difficultyMeterShowTime = wrapper
                    .builder("difficulty.meter.showDuration")
                    .comment("Show the difficulty meter for this many seconds (only on SOMETIMES mode)")
                    .defineInRange(8, 0, Double.MAX_VALUE);

            difficultyMeterAnchor = wrapper
                    .builder("difficulty.meter.position.anchor")
                    .comment("Position of the difficulty meter.", EnumValue.allValuesComment(Anchor.class))
                    .defineEnum(Anchor.BOTTOM_LEFT);
            difficultyMeterOffsetX = wrapper
                    .builder("difficulty.meter.position.offsetX")
                    .comment("Fine-tune the difficulty meter's position")
                    .defineInRange(5, Integer.MIN_VALUE, Integer.MAX_VALUE);
            difficultyMeterOffsetY = wrapper
                    .builder("difficulty.meter.position.offsetY")
                    .comment("Fine-tune the difficulty meter's position")
                    .defineInRange(-30, Integer.MIN_VALUE, Integer.MAX_VALUE);
            difficultyMeterTextScale = wrapper
                    .builder("difficulty.meter.text.scale")
                    .comment("Scale of text on the difficulty meter")
                    .defineInRange(0.6, 0, Double.MAX_VALUE);
            displayBlightEffect = wrapper
                    .builder("blights.render")
                    .comment("whether the purple flame should render on blights or not.",
                            "This does not change whether the mob is a blight, only hides the effect.")
                    .define(true);
            //endregion
        }
    }

    public static class Common {
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

        Common(ConfigSpecWrapper wrapper) {
            wrapper.comment("features",
                    "All SH features can be disabled here. False to disable.");

            crystalsAddHealth = wrapper
                    .builder("features.crystalsAddHealth")
                    .comment("Enable player bonus hp by crystals.")
                    .define(true);

            xpAddHealth = wrapper
                    .builder("features.xpAddHealth")
                    .comment("Enable player bonus hp by xp.")
                    .define(true);

            crystalsRegenHealth = wrapper
                    .builder("features.crystalsRegenHealth")
                    .comment("Enable player regen hp by crystals.")
                    .define(true);

            crystalsAddPetHealth = wrapper
                    .builder("features.crystalsAddPetHealth")
                    .comment("Enable pet add hp by crystals.")
                    .define(true);

            crystalsAddDamage = wrapper
                    .builder("features.crystalsAddDamage")
                    .comment("Enable player add damage by crystals.")
                    .define(true);

            hpCrystalsOreGen = wrapper
                    .builder("features.hpCrystalsOreGen")
                    .comment("Enable ore gen of health crystals. Still drops as loot.")
                    .define(true);

            powerCrystalsOreGen = wrapper
                    .builder("features.powerCrystalsOreGen")
                    .comment("Enable ore gen of power crystals. Still drops as loot.")
                    .define(true);

            mobHpIncrease = wrapper
                    .builder("features.mobHpIncrease")
                    .comment("Mobs will gain bonus health with difficulty.")
                    .define(true);

            mobDamageIncrease = wrapper
                    .builder("features.mobDamageIncrease")
                    .comment("Mobs will gain bonus damage with difficulty.")
                    .define(true);

            playerDamageScaling = wrapper
                    .builder("features.playerDamageScaling")
                    .comment("Enable player damage scaling.")
                    .define(true);

            mobDamageScaling = wrapper
                    .builder("features.mobDamageScaling")
                    .comment("Enable mob damage scaling.")
                    .define(true);

            enableDifficulty = wrapper
                    .builder("features.enableDifficulty")
                    .comment("Enable difficulty system. If disabled, everything will have 0 difficulty.")
                    .define(true);

            enableBlights = wrapper
                    .builder("features.enableBlights")
                    .comment("Enable blights. If disabled, no blights will spawn.")
                    .define(true);



            wrapper.comment("debug",
                    "Debug settings are intended for tuning configs or diagnosing issues.",
                    "They may decrease performance and should be disabled for normal play.");

            debugMaster = wrapper
                    .builder("debug.masterSwitch")
                    .comment("Must be true for other debug settings to apply")
                    .define(false);
            debugShowOverlay = withMasterCheck(wrapper
                    .builder("debug.showOverlay")
                    .comment("Show some text in-game about player health, difficulty, and maybe other things.")
                    .define(true));
            debugLogEntitySpawns = withMasterCheck(wrapper
                    .builder("debug.logEntitySpawns")
                    .comment("Log details of entity spawns, including effects of difficulty.",
                            "This creates a lot of log spam, and will likely lag the game.")
                    .define(false));
            debugMobPotionEffects = withMasterCheck(wrapper
                    .builder("debug.logApplyingMobPotions")
                    .comment("Logs details of potions effects added to mobs.")
                    .define(false));
            debugLogScaledDamage = withMasterCheck(wrapper
                    .builder("debug.logDamageScaling")
                    .comment("Log details of scaled damage, useful for fine-tuning damage scaling.",
                            "May create a fair amount of log spam, but shouldn't slow down the game too much.")
                    .define(true));
        }

        Supplier<Boolean> withMasterCheck(BooleanValue option) {
            return () -> debugMaster.get() && option.get();
        }
    }

    private Config() {}

    public static void init() {
        WRAPPER_CLIENT.validate();
        WRAPPER_COMMON.validate();
        GENERAL.validate();
    }

    private static Path resolve(Path parent, String path) {
        // Ensure directories exist
        File directory = parent.toFile();
        if (!directory.exists() && !directory.mkdirs()) {
            // Failed to create directories... should probably let this crash
            ScalingHealth.LOGGER.error("Failed to create config directory '{}'. This won't end well...",
                    directory.getAbsolutePath());
        }
        return parent.resolve(path);
    }
}
