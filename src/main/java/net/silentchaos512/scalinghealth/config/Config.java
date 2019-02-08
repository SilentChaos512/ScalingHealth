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

import net.minecraft.entity.Entity;
import net.minecraftforge.common.extensions.IForgeDimension;

public final class Config {
    /*
    // Per-dimension configs. The "default" dimension is stored in its own field below.
    private static final Map<Integer, DimensionConfig> DIMENSIONS = new HashMap<>();
    // config/scaling-health
    private static final Path SH_CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("scaling-health");
    // config/scaling-health/dimensions
    private static final Path DIMENSION_CONFIG_DIR = SH_CONFIG_DIR.resolve("dimensions");
    // The "default" dimension config, used when map does not contain a config for the dimension.
    private static final DimensionConfig DEFAULT = new DimensionConfig(
            resolve(DIMENSION_CONFIG_DIR, "default.toml"));

    static {
        // Load all dimension config files!
        Path path = resolve(DIMENSION_CONFIG_DIR, "");
        File directory = path.toFile();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                ScalingHealth.LOGGER.info("Found possible dimension config: {}", file);
                DimensionConfig config = new DimensionConfig(file.toPath());
                int dimensionID = config.dimensionID();
                DIMENSIONS.put(dimensionID, config);
                ScalingHealth.LOGGER.info("Loaded config for dimension {}", dimensionID);
            }
        } else {
            ScalingHealth.LOGGER.error("Something went wrong when trying to gets files from '{}'", directory);
        }
    }

    private static final ForgeConfigSpec.Builder BUILDER_CLIENT = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder BUILDER_COMMON = new ForgeConfigSpec.Builder();

    public static final Client CLIENT = new Client(BUILDER_CLIENT);
    public static final Common COMMON = new Common(BUILDER_COMMON);

    public static class Client {
        // Health Icons
        public final Supplier<HeartIconStyle> heartIconStyle;
        public final BooleanValue firstRowVanillaStyle;
        public final ColorList heartColors;
        public final BooleanValue lastHeartOutline;
        public final LazyLoadBase<Color> lastHeartOutlineColor;

        // Health Text
        public final Supplier<HealthTextStyle> healthTextStyle;
        public final IntValue healthTextOffsetX;
        public final IntValue healthTextOffsetY;
        public final Supplier<HealthTextColor> healthTextColorStyle;
        public final LazyLoadBase<Color> healthTextFullColor;
        public final LazyLoadBase<Color> healthTextEmptyColor;

        // Absorption Icons
        public final Supplier<AbsorptionIconStyle> absorptionIconStyle;
        public final ColorList absorptionHeartColors;

        // Absorption Text
        public final Supplier<HealthTextStyle> absorptionTextStyle;
        public final IntValue absorptionTextOffsetX;
        public final IntValue absorptionTextOffsetY;
        public final LazyLoadBase<Color> absorptionTextColor;

        // Difficulty
        public final BooleanValue warnWhenSleeping;
        // Difficulty Meter
        public final Supplier<DifficultyMeterShow> difficultyMeterShow;
        public final DoubleValue difficultyMeterShowTime;
        public final Supplier<HudAnchor> difficultyMeterAnchor;
        public final IntValue difficultyMeterOffsetX;
        public final IntValue difficultyMeterOffsetY;

        Client(ForgeConfigSpec.Builder builder) {
            //region Health Hearts

            builder.comment("Settings for heart rows");
            builder.push("hearts.health.icons");

            heartIconStyle = EnumUtils.defineEnumFix(builder, "style", HeartIconStyle.REPLACE);
            firstRowVanillaStyle = builder
                    .comment("The first hearts row is render using vanilla textures")
                    .define("firstRowVanillaStyle", false);
            heartColors = new ColorList(builder, "colors",
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
                    .define("lastHeartOutline", true);
            lastHeartOutlineColor = Color.define(builder,
                    "lastHeartOutlineColor",
                    0xFFFFFF,
                    "The color of the last heart outline, if enabled (see lastHeartOutline)");

            builder.pop(); // icons

            builder.comment("Settings for the text displayed next to the heart rows");
            builder.push("text");

            healthTextStyle = EnumUtils.defineEnumFix(builder, "style", HealthTextStyle.ROWS);
            healthTextOffsetX = builder
                    .comment("Fine-tune text position")
                    .defineInRange("offsetX", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            healthTextOffsetY = builder
                    .comment("Fine-tune text position")
                    .defineInRange("offsetY", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

            builder.push("color");

            healthTextColorStyle = EnumUtils.defineEnumFix(builder, "style", HealthTextColor.TRANSITION);
            healthTextFullColor = Color.define(builder, "full", 0x4CFF4C, "Color when health is full or style is not TRANSITION");
            healthTextEmptyColor = Color.define(builder, "empty", 0xFF4C4C, "Color when health is empty and style is TRANSITION");

            builder.pop(); // color
            builder.pop(); // text
            builder.pop(); // health
            //endregion

            //region Absorption Hearts

            builder.push("absorption.icons");

            absorptionIconStyle = EnumUtils.defineEnumFix(builder, "style", AbsorptionIconStyle.SHIELD);
            absorptionHeartColors = new ColorList(builder, "colors",
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

            builder.pop(); // icons

            builder.push("text");

            absorptionTextStyle = EnumUtils.defineEnumFix(builder, "style", HealthTextStyle.DISABLED,
                    ImmutableSet.of(HealthTextStyle.DISABLED, HealthTextStyle.HEALTH_ONLY, HealthTextStyle.ROWS));
            absorptionTextOffsetX = builder
                    .comment("Fine-tune text position")
                    .defineInRange("offsetX", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            absorptionTextOffsetY = builder
                    .comment("Fine-tune text position")
                    .defineInRange("offsetY", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            absorptionTextColor = Color.define(builder, "color", 0xFFFFFF, "The color of the absorption text");

            builder.pop(); // text
            builder.pop(); // absorption
            builder.pop(); // hearts
            //endregion

            //region Difficulty

            builder.push("difficulty");

            warnWhenSleeping = builder
                    .comment("Display a warning to players trying to sleep, to remind them their difficulty may change. Sleeping is still allowed.")
                    .define("warnWhenSleeping", true);

            builder.push("meter");

            difficultyMeterShow = EnumUtils.defineEnumFix(builder, "show", DifficultyMeterShow.SOMETIMES);
            difficultyMeterShowTime = builder
                    .comment("Show the difficulty meter for this many seconds (only on SOMETIMES mode)")
                    .defineInRange("showDuration", 8, 0, Double.MAX_VALUE);

            builder.push("position");
            difficultyMeterAnchor = EnumUtils.defineEnumFix(builder, "anchor", HudAnchor.BOTTOM_LEFT);
            difficultyMeterOffsetX = builder
                    .defineInRange("offsetX", 5, Integer.MIN_VALUE, Integer.MAX_VALUE);
            difficultyMeterOffsetY = builder
                    .defineInRange("offsetY", -30, Integer.MIN_VALUE, Integer.MAX_VALUE);

            builder.pop(); // position
            builder.pop(); // meter
            builder.pop(); // difficulty

            //endregion
        }
    }

    public static class Common {
        public final BooleanValue debugMaster;
        public final BooleanValue debugShowOverlay;
        public final BooleanValue debugLogEntitySpawns;
        public final BooleanValue debugLogScaledDamage;

        Common(ForgeConfigSpec.Builder builder) {
            builder.comment("Debug settings are intended for tuning configs or diagnosing issues.",
                    "They may decrease performance and should be disabled for normal play.");
            builder.push("debug");

            debugMaster = builder
                    .comment("Must be true for other debug settings to apply")
                    .define("masterSwitch", false);
            debugShowOverlay = builder
                    .comment("Show some text in-game about player health, difficulty, and maybe other things.")
                    .define("showOverlay", true);
            debugLogEntitySpawns = builder
                    .comment("Log details of entity spawns, including effects of difficulty.",
                            "This creates a lot of log spam, and will likely lag the game.")
                    .define("logEntitySpawns", false);
            debugLogScaledDamage = builder
                    .comment("Log details of scaled damage, useful for fine-tuning damage scaling.",
                            "May create a fair amount of log spam, but shouldn't slow down the game too much.")
                    .define("logDamageScaling", true);

            builder.pop(); // debug
        }
    }

    private static final ForgeConfigSpec SPEC_CLIENT = BUILDER_CLIENT.build();
    private static final ForgeConfigSpec SPEC_COMMON = BUILDER_COMMON.build();

    private Config() {}

    public static void register(FMLModLoadingContext ctx) {
        ctx.registerConfig(ModConfig.Type.CLIENT, SPEC_CLIENT);
        ctx.registerConfig(ModConfig.Type.SERVER, SPEC_COMMON);
    }

//    public static void load() {
//        Path clientFile = resolve(SH_CONFIG_DIR, "client.toml");
//        SPEC_CLIENT.setConfigFile(clientFile);
//        ScalingHealth.LOGGER.debug("Loaded client config from {}", clientFile);
//
//        Path commonFile = resolve(SH_CONFIG_DIR, "common.toml");
//        SPEC_COMMON.setConfigFile(commonFile);
//        ScalingHealth.LOGGER.debug("Loaded common config from {}", commonFile);
//    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        ScalingHealth.LOGGER.debug("Loaded config file {}", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.ConfigReloading configEvent) {
        ScalingHealth.LOGGER.fatal("Config just got changed on the file system!");
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
    */

    private static final DimensionConfig TEMP = new DimensionConfig();

    public static DimensionConfig get(IForgeDimension dimension) {
        return get(dimension.getId());
    }

    public static DimensionConfig get(Entity entity) {
        return get(entity.dimension);
    }

    public static DimensionConfig get(int dimension) {
//        return DIMENSIONS.getOrDefault(dimension, DEFAULT);
        return TEMP;
    }
}
