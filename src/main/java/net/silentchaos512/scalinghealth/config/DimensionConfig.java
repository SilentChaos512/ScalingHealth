package net.silentchaos512.scalinghealth.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.google.common.collect.ImmutableList;
import com.udojava.evalex.Expression;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.lib.AreaDifficultyMode;
import net.silentchaos512.scalinghealth.lib.MobHealthMode;
import net.silentchaos512.utils.config.*;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class DimensionConfig {
    public static class General {
        General(ConfigSpecWrapper wrapper) {
        }
    }

    public static class Items {
        public final DoubleValue cursedHeartAffect;
        public final DoubleValue enchantedHeartAffect;
        public final DoubleValue heartContainerHealthRestored;
        public final IntValue heartContainerLevelCost;
        public final BooleanValue heartContainerIncreaseHealth;

        Items(ConfigSpecWrapper wrapper) {
            wrapper.comment("items", "Settings for items when used in this dimension");

            cursedHeartAffect = wrapper
                    .builder("item.cursedHeart.change")
                    .comment("Change in difficulty when a cursed heart is used")
                    .defineInRange(10, -Double.MAX_VALUE, Double.MAX_VALUE);
            enchantedHeartAffect = wrapper
                    .builder("item.enchantedHeart.change")
                    .comment("Change in difficulty when an enchanted heart is used")
                    .defineInRange(-10, -Double.MAX_VALUE, Double.MAX_VALUE);
            heartContainerHealthRestored = wrapper
                    .builder("item.heartContainer.healthRestored")
                    .comment("The amount of additional health restored by heart containers (min = 0)",
                            "Heart containers always restore the amount of health they add, this is a bonus")
                    .defineInRange(4, 0, Double.MAX_VALUE);
            heartContainerLevelCost = wrapper
                    .builder("item.heartContainer.levelCost")
                    .comment("The number of levels required to use a heart container (min = 0)")
                    .defineInRange(3, 0, Integer.MAX_VALUE);
            heartContainerIncreaseHealth = wrapper
                    .builder("item.heartContainer.increaseHealth")
                    .comment("Do heart containers increase max health?",
                            "If set to false, they can still be used as a healing item.")
                    .define(true);
        }
    }

    public static class Pets {
        public final DoubleValue regenDelay;

        Pets(ConfigSpecWrapper wrapper) {
            wrapper.comment("pets", "Settings for tamed creatures");

            regenDelay = wrapper
                    .builder("pets.regenDelay")
                    .comment("Delay (in seconds) between regen ticks for pets. Set 0 to disable.")
                    .defineInRange(30, 0, Double.MAX_VALUE);
        }
    }

    public static class Player {
        // Health settings
        public final BooleanValue allowHealthModification;
        public final BooleanValue localHealth;
        public final IntValue startingHealth;
        public final IntValue minHealth;
        public final IntValue maxHealth;
        public final Supplier<Expression> setOnDeath;

        // TODO: regen configs

        Player(ConfigSpecWrapper wrapper) {
            wrapper.comment("player", "Settings for players");

            allowHealthModification = wrapper
                    .builder("player.health.allowModification")
                    .comment("Allow Scaling Health to apply health modifiers. Heart Containers will not work if this is disabled.")
                    .define(true);
            localHealth = wrapper
                    .builder("player.health.localHealth")
                    .comment("Player health and max health are unique to this dimension.")
                    .define(false);
            startingHealth = wrapper
                    .builder("player.health.startingHealth")
                    .comment("Starting player health, in half-hearts (20 = 10 hearts)")
                    .defineInRange(20, 2, Integer.MAX_VALUE);
            minHealth = wrapper
                    .builder("player.health.minHealth")
                    .comment("The minimum amount of health a player can have (this can be lower than starting health)")
                    .defineInRange(2, 2, Integer.MAX_VALUE);
            maxHealth = wrapper
                    .builder("player.health.maxHealth")
                    .comment("The highest max health a player can reach, not considering the vanilla health cap and modifiers from other sources. 0 means 'unlimited'.")
                    .defineInRange(0, 0, Integer.MAX_VALUE);
            setOnDeath = defineExpression(wrapper,
                    "setOnDeath",
                    EvalVars.MAX_HEALTH.varName(),
                    EvalVars.MAX_HEALTH,
                    "On death, set the player's max health to this value. By default, there is no change.");
        }
    }

    public static class Mobs {
        public final EnumValue<MobHealthMode> healthMode;

        Mobs(ConfigSpecWrapper wrapper) {
            wrapper.comment("mob.health", "Mob health settings");

            healthMode = wrapper
                    .builder("mob.health.modifierMode")
                    .comment("Determines how difficulty affects mob health.",
                            "TODO: Describe each mode")
                    .defineEnum(MobHealthMode.MULTI_HALF);
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
        public final EnumValue<AreaDifficultyMode> areaMode;
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

        private final ConfigSpec byEntitySpec = new ConfigSpec();
        private final CommentedConfig byEntityDefault = CommentedConfig.inMemory();

        Difficulty(ConfigSpecWrapper wrapper) {
            byEntitySpec.defineList("types", ImmutableList.of("minecraft:villager"), o -> o instanceof String);
            byEntitySpec.define("onKilled", "difficulty + 0.01", o -> validateExpression(o, "onKilled", EvalVars.PLAYER_DIFFICULTY));

            wrapper.comment("difficulty", "Settings related to difficulty. Difficulty determines various things, such as how much health mobs have.");

            difficultyExempt = wrapper
                    .builder("difficulty.exemptPlayers")
                    .comment("These players will not gain difficulty. Use either name or UUID.",
                            "Note: if non-exempt players are nearby, mobs will still be stronger.")
                    .defineList(ImmutableList.of(), ConfigValue.IS_NONEMPTY_STRING);
            startingValue = wrapper
                    .builder("difficulty.startingValue")
                    .comment("The initial player difficulty value for newly spawned players.")
                    .defineInRange(0, 0, Double.MAX_VALUE);
            minValue = wrapper
                    .builder("difficulty.minValue")
                    .comment("The minimum difficulty value a player can have. This can be smaller from startingValue.")
                    .defineInRange(0, 0, Double.MAX_VALUE);
            maxValue = wrapper
                    .builder("difficulty.maxValue")
                    .comment("The maximum difficulty value a player can have.")
                    .defineInRange(250, 0, Double.MAX_VALUE);
            changePerSecond = wrapper
                    .builder("difficulty.changePerSecond")
                    .comment("Every second, this value is added to player and dimension difficulty.",
                            "Enter a negative number to subtract difficulty instead.")
                    .defineInRange(0.0011575, -10000, 10000);
            distanceFactor = wrapper
                    .builder("difficulty.distanceFactor")
                    .comment("Distance-based area modes will multiply distance by this value to get difficulty")
                    .defineInRange(0.0025, -100, 100);
            areaMode = wrapper
                    .builder("difficulty.areaMode")
                    .comment("Determines how difficulty is calculated.",
                            "TODO: List and describe values")
                    .defineEnum(AreaDifficultyMode.WEIGHTED_AVERAGE);
            searchRadius = wrapper
                    .builder("difficulty.searchRadius")
                    .comment("Distance to look for difficulty sources (players) when calculating area difficulty.")
                    .defineInRange(256, 64, Integer.MAX_VALUE);
            localPlayerDifficulty = wrapper
                    .builder("difficulty.localPlayerDifficulty")
                    .comment("If true, player difficulty is tracked for this dimension.",
                            "Otherwise, the value tracked for the \"default\" dimension is used.")
                    .define(false);
            localDimensionDifficulty = wrapper
                    .builder("difficulty.localDimensionDifficulty")
                    .comment("Track a difficulty value for this dimension. Otherwise, uses the \"default\" dimension.")
                    .define(false);
            ignoreYAxis = wrapper
                    .builder("difficulty.ignoreYAxis")
                    .comment("Ignore the Y-axis in difficulty calculations")
                    .define(true);
            groupAreaBonus = defineExpression(wrapper,
                    "difficulty.groupAreaBonus",
                    "1 + 0.05 * (areaPlayerCount - 1)",
                    EvalVars.AREA_PLAYER_COUNT,
                    "A multiplier for area difficulty calculations, typically based on the number of players in the search radius.");
            idleMultiplier = defineExpression(wrapper,
                    "difficulty.idleMultiplier",
                    "0.75",
                    null,
                    "Multiplier for changePerSecond when the player is not moving.");

            // Mutators
            wrapper.comment("difficulty.mutators", "Change difficulty when certain things happen");

            onBlightKilled = defineExpression(wrapper,
                    "difficulty.mutators.onBlightKilled",
                    EvalVars.PLAYER_DIFFICULTY.varName(),
                    EvalVars.PLAYER_DIFFICULTY,
                    "A blight is killed by a player");
            onBossKilled = defineExpression(wrapper,
                    "difficulty.mutators.onBossKilled",
                    EvalVars.PLAYER_DIFFICULTY.varName(),
                    EvalVars.PLAYER_DIFFICULTY,
                    "A boss is killed by a player");
            onHostileKilled = defineExpression(wrapper,
                    "difficulty.mutators.onHostileKilled",
                    EvalVars.PLAYER_DIFFICULTY.varName(),
                    EvalVars.PLAYER_DIFFICULTY,
                    "A normal hostile mob is killed by a player");
            onPeacefulKilled = defineExpression(wrapper,
                    "difficulty.mutators.onPeacefulKilled",
                    EvalVars.PLAYER_DIFFICULTY.varName(),
                    EvalVars.PLAYER_DIFFICULTY,
                    "A peaceful mob is killed by a player");
            onPlayerKilled = defineExpression(wrapper,
                    "difficulty.mutators.onPlayerKilled",
                    EvalVars.PLAYER_DIFFICULTY.varName() + " + 1.0",
                    EvalVars.PLAYER_DIFFICULTY,
                    "A player killed another player");
            onPlayerDeath = defineExpression(wrapper,
                    "difficulty.mutators.onPlayerDeath",
                    EvalVars.PLAYER_DIFFICULTY.varName(),
                    EvalVars.PLAYER_DIFFICULTY,
                    "A player dies");
            onPlayerSleep = defineExpression(wrapper,
                    "difficulty.mutators.onPlayerSleep",
                    EvalVars.PLAYER_DIFFICULTY.varName(),
                    EvalVars.PLAYER_DIFFICULTY,
                    "A player sleeps in a bed");

            byEntitySpec.correct(byEntityDefault);

            // byEntity table list
            byEntityMutators = wrapper
                    .builder("difficulty.mutators.byEntity")
                    .defineList(ImmutableList.of(byEntityDefault), o -> {
                        if (!(o instanceof CommentedConfig)) return false;
                        return byEntitySpec.isCorrect((CommentedConfig) o);
                    });
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

    private static Supplier<Expression> defineExpression(ConfigSpecWrapper wrapper, String path, String defaultValue, @Nullable EvalVars intendedVar, String comment) {
        StringValue config = wrapper
                .builder(path)
                .comment("EvalEx expression: " + comment)
                .defineString(() -> defaultValue, o -> validateExpression(o, path, intendedVar));

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

    private final ConfigSpecWrapper wrapper;

    public final General general;
    public final Items item;
    public final Player player;
    public final Pets pets;
    public final Difficulty difficulty;

    private final String configFileName;
    private final int dimensionId;

    @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
    DimensionConfig(final Path path) {
        wrapper = ConfigSpecWrapper.create(path);
        general = new General(wrapper);
        item = new Items(wrapper);
        player = new Player(wrapper);
        pets = new Pets(wrapper);
        difficulty = new Difficulty(wrapper);

        configFileName = path.getFileName().toString();
        String trimmed = configFileName
                .replaceFirst("\\.toml$", "")
                .replaceFirst("^dim(ension)?_?", "");
        this.dimensionId = dimensionIdFromString(trimmed);
    }

    void validate() {
        wrapper.validate();
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
