package net.silentchaos512.scalinghealth.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.common.collect.ImmutableList;
import com.udojava.evalex.Expression;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.silentchaos512.lib.util.EnumUtils;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.lib.AreaDifficultyMode;
import net.silentchaos512.scalinghealth.lib.MobHealthMode;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class DimensionConfig {
    public static class General {
        General(ForgeConfigSpec.Builder builder) {
        }
    }

    public static class Pets {
        public final DoubleValue regenDelay;

        Pets(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for tamed creatures");
            builder.push("pets");

            regenDelay = builder
                    .comment("Delay (in seconds) between regen ticks for pets. Set 0 to disable.")
                    .defineInRange("regenDelay", 30, 0, Double.MAX_VALUE);

            builder.pop(); // pets
        }
    }

    public static class Player {
        // Health settings
        public final BooleanValue allowHealthModification;
        public final BooleanValue localHealth;
        public final IntValue startingHealth;
        public final IntValue maxHealth;
        public final Supplier<Expression> setOnDeath;

        // TODO: regen configs

        Player(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for players");
            builder.push("player");

            builder.push("health");
            allowHealthModification = builder
                    .comment("Allow Scaling Health to apply health modifiers. Heart Containers will not work if this is disabled.")
                    .define("allowModification", true);
            localHealth = builder
                    .comment("Player health and max health are unique to this dimension.")
                    .define("localHealth", false);
            startingHealth = builder
                    .comment("Starting player health, in half-hearts (20 = 10 hearts)")
                    .defineInRange("startingHealth", 20, 2, Integer.MAX_VALUE);
            maxHealth = builder
                    .comment("The highest max health a player can reach, not considering the vanilla health cap and modifiers from other sources. 0 means 'unlimited'.")
                    .defineInRange("maxHealth", 0, 0, Integer.MAX_VALUE);
            setOnDeath = defineExpression(builder,
                    "setOnDeath",
                    EvalVars.MAX_HEALTH.varName(),
                    EvalVars.MAX_HEALTH,
                    "On death, set the player's max health to this value. By default, there is no change.");
            builder.pop(); // health

            builder.pop(); // player
        }
    }

    public static class Mobs {
        public final Supplier<MobHealthMode> healthMode;

        Mobs(ForgeConfigSpec.Builder builder) {
            builder.comment("Mob health settings");
            builder.push("mob.health");

            healthMode = EnumUtils.defineEnumFix(builder, "modifierMode", MobHealthMode.MULTI_HALF);

            builder.pop(); // health
            builder.pop(); // mob
        }
    }

    public static class Difficulty {
        // Standard options
        final ConfigValue<List<? extends String>> difficultyExempt;
        public final DoubleValue startingValue;
        public final DoubleValue minValue;
        public final DoubleValue maxValue;
        public final DoubleValue changePerSecond;
        public final DoubleValue distanceFactor;
        public final Supplier<AreaDifficultyMode> areaMode;
        public final IntValue searchRadius;
        public final BooleanValue localPlayerDifficulty;
        public final BooleanValue localDimensionDifficulty;
        public final BooleanValue ignoreYAxis;
        public final Supplier<Expression> groupAreaBonus;
        public final Supplier<Expression> idleMultiplier;

        // Mutators
        public final Supplier<Expression> onBlightKilled;
        public final Supplier<Expression> onBossKilled;
        public final Supplier<Expression> onHostileKilled;
        public final Supplier<Expression> onPeacefulKilled;
        public final Supplier<Expression> onPlayerKilled;
        public final Supplier<Expression> onPlayerDeath;
        public final Supplier<Expression> onPlayerSleep;
        private final ConfigValue<List<? extends CommentedConfig>> byEntityMutators;

        private final ForgeConfigSpec byEntitySpec = new ForgeConfigSpec.Builder()
                .defineList("types", ImmutableList.of("minecraft:villager"), o -> o instanceof String).next()
                .define("onKilled", "difficulty + 0.01", o -> validateExpression(o, "onKilled", EvalVars.PLAYER_DIFFICULTY)).next()
                .build();
        private final CommentedConfig byEntityDefault = CommentedConfig.inMemory();

        Difficulty(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings related to difficulty. Difficulty determines various things, such as how much health mobs have.");
            builder.push("difficulty");

            difficultyExempt = builder
                    .comment("These players will not gain difficulty. Use either name or UUID.",
                            "Note: if non-exempt players are nearby, mobs will still be stronger.")
                    .defineList("exemptPlayers", ImmutableList.of(), o -> o instanceof String);
            startingValue = builder
                    .comment("The initial player difficulty value for newly spawned players.")
                    .defineInRange("startingValue", 0, 0, Double.MAX_VALUE);
            minValue = builder
                    .comment("The minimum difficulty value a player can have. This can be smaller from startingValue.")
                    .defineInRange("minValue", 0, 0, Double.MAX_VALUE);
            maxValue = builder
                    .comment("The maximum difficulty value a player can have.")
                    .defineInRange("maxValue", 250, 0, Double.MAX_VALUE);
            changePerSecond = builder
                    .comment("Every second, this value is added to player and dimension difficulty.",
                            "Enter a negative number to subtract difficulty instead.")
                    .defineInRange("changePerSecond", 0.0011575, -10000, 10000);
            distanceFactor = builder
                    .comment("Distance-based area modes will multiply distance by this value to get difficulty")
                    .defineInRange("distanceFactor", 0.0025, -100, 100);
            areaMode = EnumUtils.defineEnumFix(builder, "areaMode", AreaDifficultyMode.WEIGHTED_AVERAGE);
            searchRadius = builder
                    .comment("Distance to look for difficulty sources (players) when calculating area difficulty.")
                    .defineInRange("searchRadius", 256, 64, Integer.MAX_VALUE);
            localPlayerDifficulty = builder
                    .comment("If true, player difficulty is tracked for this dimension.",
                            "Otherwise, the value tracked for the \"default\" dimension is used.")
                    .define("localPlayerDifficulty", false);
            localDimensionDifficulty = builder
                    .comment("Track a difficulty value for this dimension. Otherwise, uses the \"default\" dimension.")
                    .define("localDimensionDifficulty", false);
            ignoreYAxis = builder
                    .comment("Ignore the Y-axis in difficulty calculations")
                    .define("ignoreYAxis", true);
            groupAreaBonus = defineExpression(builder,
                    "groupAreaBonus",
                    "1 + 0.05 * (areaPlayerCount - 1)",
                    EvalVars.AREA_PLAYER_COUNT,
                    "A multiplier for area difficulty calculations, typically based on the number of players in the search radius.");
            idleMultiplier = defineExpression(builder,
                    "idleMultiplier",
                    "0.75",
                    null,
                    "Multiplier for changePerSecond when the player is not moving.");

            // Mutators
            builder.comment("Change difficulty when certain things happen")
                    .push("mutators");

            onBlightKilled = defineExpression(builder,
                    "onBlightKilled",
                    EvalVars.PLAYER_DIFFICULTY.varName(),
                    EvalVars.PLAYER_DIFFICULTY,
                    "A blight is killed by a player");
            onBossKilled = defineExpression(builder,
                    "onBossKilled",
                    EvalVars.PLAYER_DIFFICULTY.varName(),
                    EvalVars.PLAYER_DIFFICULTY,
                    "A boss is killed by a player");
            onHostileKilled = defineExpression(builder,
                    "onHostileKilled",
                    EvalVars.PLAYER_DIFFICULTY.varName(),
                    EvalVars.PLAYER_DIFFICULTY,
                    "A normal hostile mob is killed by a player");
            onPeacefulKilled = defineExpression(builder,
                    "onPeacefulKilled",
                    EvalVars.PLAYER_DIFFICULTY.varName(),
                    EvalVars.PLAYER_DIFFICULTY,
                    "A peaceful mob is killed by a player");
            onPlayerKilled = defineExpression(builder,
                    "onPlayerKilled",
                    EvalVars.PLAYER_DIFFICULTY.varName() + " + 1.0",
                    EvalVars.PLAYER_DIFFICULTY,
                    "A player killed another player");
            onPlayerDeath = defineExpression(builder,
                    "onPlayerDeath",
                    EvalVars.PLAYER_DIFFICULTY.varName(),
                    EvalVars.PLAYER_DIFFICULTY,
                    "A player dies");
            onPlayerSleep = defineExpression(builder,
                    "onPlayerSleep",
                    EvalVars.PLAYER_DIFFICULTY.varName(),
                    EvalVars.PLAYER_DIFFICULTY,
                    "A player sleeps in a bed");

            byEntitySpec.correct(byEntityDefault);

            // byEntity table list
            byEntityMutators = builder
                    .defineList("byEntity", ImmutableList.of(byEntityDefault), o -> {
                        if (!(o instanceof CommentedConfig)) return false;
                        return byEntitySpec.isCorrect((CommentedConfig) o);
                    });

            builder.pop(); // mutators

            builder.pop(); // difficulty
        }

        /**
         * Check if player is difficulty exempt. Checks name (ignoring case) and UUID.
         *
         * @return True if exempt, false otherwise.
         */
        public boolean isExempt(EntityPlayer player) {
            List<? extends String> list = difficultyExempt.get();
            for (String value : list) {
                if (value.equalsIgnoreCase(player.getName().getFormattedText())) {
                    return true;
                }
            }
            return list.contains(player.getUniqueID().toString());
        }

        public Expression getKillMutator(EntityLivingBase entity) {
            // Might be nice if there was a more generic way to handle lists of tables.
            // But this works for now.
            String name = Objects.requireNonNull(entity.getType().getRegistryName()).toString();
            return byEntityMutators.get().stream()
                    .filter(c -> c.<List<? extends String>>get("types").contains(name))
                    .findFirst()
                    .map(c -> new Expression(c.get("onKilled")))
                    .orElseGet(() -> getDefaultKillMutator(entity));
        }

        @SuppressWarnings("ChainOfInstanceofChecks")
        public Expression getDefaultKillMutator(EntityLivingBase entity) {
            // TODO: Check blight
            if (!entity.isNonBoss()) return onBossKilled.get();
            if (entity instanceof IMob) return onHostileKilled.get();
            if (entity instanceof IAnimal) return onPeacefulKilled.get();
            if (entity instanceof EntityPlayer) return onPlayerKilled.get();
            return onHostileKilled.get();
        }
    }

    private static Supplier<Expression> defineExpression(ForgeConfigSpec.Builder builder, String path, String defaultValue, @Nullable EvalVars intendedVar, String comment) {
        ConfigValue<String> config = builder
                .comment("EvalEx expression: " + comment)
                .define(path, () -> defaultValue, o -> validateExpression(o, path, intendedVar));

        // TODO: I would guess creating an Expression is very expensive. Is this OK?
        // Expression creation time: roughly 5000 ns (0.005 ms)
        // As long as it's not referenced every render tick, it should be OK
        // Could use LazyLoadBase, but would that affect config reloads?
        return () -> new Expression(config.get());
    }

    private static boolean validateExpression(Object obj, String path, @Nullable EvalVars intendedVar) {
        if (!(obj instanceof String)) return false;
        String str = (String) obj;

        if (str.isEmpty()) {
            ScalingHealth.LOGGER.warn("Empty expression for '{}'", path);
            return false;
        }

        if (intendedVar != null && !str.contains(intendedVar.varName())) {
            ScalingHealth.LOGGER.warn(
                    "Expression for '{}' does not contain the '{}' variable. This could be intended, but seems odd.",
                    path, intendedVar);
        }

        // TODO: What else can we do here?
        return true;
    }

    private final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

    public final General general = new General(builder);
    public final Player player = new Player(builder);
    public final Pets pets = new Pets(builder);
    public final Difficulty difficulty = new Difficulty(builder);

    private final ForgeConfigSpec spec = builder.build();

    private final String configFileName;
    private final int dimensionId;

    @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
    DimensionConfig(final Path path) {
//        spec.setConfigFile(path);

        configFileName = path.getFileName().toString();
        String trimmed = configFileName
                .replaceFirst("\\.toml$", "")
                .replaceFirst("^dim(ension)?_?", "");
        this.dimensionId = dimensionIdFromString(trimmed);
    }

    int dimensionID() {
        return dimensionId;
    }

    private static int dimensionIdFromString(String str) {
        if ("default".equalsIgnoreCase(str) || "overworld".equalsIgnoreCase(str)) return 0;
        if ("nether".equalsIgnoreCase(str)) return -1;
        if ("end".equalsIgnoreCase(str)) return 1;

        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            ScalingHealth.LOGGER.error("Unknown dimension ID: '{}'", str);
            return 0;
        }
    }

    @Override
    public String toString() {
        return "DimensionConfig{" +
                "id=" + dimensionId +
                ", fileName=" + configFileName +
                "}";
    }
}
