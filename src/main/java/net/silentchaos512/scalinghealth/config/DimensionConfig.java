package net.silentchaos512.scalinghealth.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.google.common.collect.ImmutableList;
import com.udojava.evalex.Expression;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.silentchaos512.lib.util.BiomeUtils;
import net.silentchaos512.lib.util.DimensionUtils;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.event.BlightHandler;
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
        public final DoubleValue chanceHeartAffect;
        public final DoubleValue heartCrystalHealthRestored;
        public final IntValue heartCrystalLevelCost;
        public final BooleanValue heartCrystalIncreaseHealth;
        public final IntValue powerCrystalLevelCost;
        public final DoubleValue powerCrystalDamageIncrease;

        Items(ConfigSpecWrapper wrapper) {
            wrapper.comment("items", "Settings for items when used in this dimension");

            cursedHeartAffect = wrapper
                    .builder("item.cursed_heart.change")
                    .comment("Change in difficulty when a cursed heart is used")
                    .defineInRange(10, -Double.MAX_VALUE, Double.MAX_VALUE);
            enchantedHeartAffect = wrapper
                    .builder("item.enchanted_heart.change")
                    .comment("Change in difficulty when an enchanted heart is used")
                    .defineInRange(-10, -Double.MAX_VALUE, Double.MAX_VALUE);
            chanceHeartAffect = wrapper
                    .builder("item.chance_heart.change")
                    .comment("Change in difficulty when a chance heart is used",
                            "For a value n, a chance heart has 1 in 2n + 1 chance of being cursed",
                            "In that case, n difficulty is added (n = 10, 1 in 21 chance to get +10)",
                            "There's a 2 in 2n + 1 chance for 1 to n difficulty to be subtracted (n = 3, 2 in 7 chance of getting -1, -2, or -3")
                    .defineInRange(10, -Double.MAX_VALUE, Double.MAX_VALUE);
            heartCrystalHealthRestored = wrapper
                    .builder("item.heart_crystal.healthRestored")
                    .comment("The amount of additional health restored by heart crystals (min = 0)",
                            "Heart crystals always restore the amount of health they add, this is a bonus")
                    .defineInRange(4, 0, Double.MAX_VALUE);
            heartCrystalLevelCost = wrapper
                    .builder("item.heart_crystal.levelCost")
                    .comment("The number of levels required to use a heart crystal (min = 0)")
                    .defineInRange(3, 0, Integer.MAX_VALUE);
            heartCrystalIncreaseHealth = wrapper
                    .builder("item.heart_crystal.increaseHealth")
                    .comment("Do heart crystals increase max health?",
                            "If set to false, they can still be used as a healing item.")
                    .define(true);
            powerCrystalLevelCost = wrapper
                    .builder("item.power_crystal.levelCost")
                    .comment("The number of levels required to use a power crystal (min = 0)")
                    .defineInRange(3, 0, Integer.MAX_VALUE);
            powerCrystalDamageIncrease = wrapper
                    .builder("item.power_crystal.damageBoost")
                    .comment("How much more damage a player deals after using a power crystal (set 0 to disable)")
                    .defineInRange(0.5, 0, Double.MAX_VALUE);
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
        public final IntValue maxAttackDamage;
        public final Supplier<Expression> setHealthOnDeath;
        public final RegenConfig regen;

        Player(ConfigSpecWrapper wrapper) {
            wrapper.comment("player", "Settings for players");

            allowHealthModification = wrapper
                    .builder("player.health.allowModification")
                    .comment("Allow Scaling Health to apply health modifiers. Heart crystals will not work if this is disabled.")
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
                    .comment("The highest max health a player can reach, not considering the vanilla",
                            " health cap and modifiers from other sources. 0 means 'unlimited'.")
                    .defineInRange(0, 0, Integer.MAX_VALUE);
            setHealthOnDeath = defineExpression(wrapper,
                    "player.health.setOnDeath",
                    EvalVars.MAX_HEALTH.varName(),
                    EvalVars.MAX_HEALTH,
                    "On death, set the player's max health to this value. By default, there is no change.");
            maxAttackDamage = wrapper
                    .builder("player.attackDamage.maxValue")
                    .comment("The highest attack damage a player can achieve, not considering the vanilla",
                            "cap and modifiers from other sources. 0 means 'unlimited'.")
                    .defineInRange(0, 0, Integer.MAX_VALUE);

            regen = RegenConfig.init(wrapper, "player.regen", true);
        }
    }

    public static class Mobs {
        final ConfigValue<List<? extends String>> mobsExempt;
        public final EnumValue<MobHealthMode> healthMode;
        public final DoubleValue hostilePotionChance;
        public final DoubleValue peacefulPotionChance;
        public final DoubleValue damageBoostScale;
        public final DoubleValue maxDamageBoost;
        public final MobPotionConfig randomPotions;
        // Blights
//        public final Supplier<Expression> blightChance;
        public final MobPotionConfig blightPotions;

        Mobs(ConfigSpecWrapper wrapper) {
            wrapper.comment("mob.health", "Mob health settings");

            healthMode = wrapper
                    .builder("mob.health.modifierMode")
                    .comment("Determines how difficulty affects mob health.",
                            "2 types of modes: Multiplier and additive, with 3 different multipliers.",
                            "MULTI: Mob hp is multiplied, mobs with higher base hp have more increase.",
                            "MULTI_HALF: Same as MULTI but mobs with more than 20 hp have reduced scaling (bosses, endermen, witches, etc).",
                            "MULTI_QUARTER: Same as MULTI_HALF but scaling is reduced further for 20hp+ mobs.",
                            "ADD: Flat increase for all mobs, no matter their base hp."
                            )
                    .defineEnum(MobHealthMode.MULTI_HALF);
            wrapper.comment("mob.potionChance", "Chance for mobs to receive a random potion effect (assuming their difficulty is high enough)");
            hostilePotionChance = wrapper
                    .builder("mob.potionChance.hostile")
                    .defineInRange(0.375, 0, 1);
            peacefulPotionChance = wrapper
                    .builder("mob.potionChance.peaceful")
                    .defineInRange(0.025, 0, 1);
            damageBoostScale = wrapper
                    .builder("mob.damage.boostScale")
                    .comment("How rapidly mob attack damage rises with difficulty (0 = no damage boost)")
                    .defineInRange(0.1, 0, Double.MAX_VALUE);
            maxDamageBoost = wrapper
                    .builder("mob.damage.maxBoost")
                    .comment("The maximum extra attack damage a mob can receive")
                    .defineInRange(10, 0, Double.MAX_VALUE);
            randomPotions = MobPotionConfig.init(wrapper, "mob.randomPotionEffects", true, ImmutableList.<CommentedConfig>builder()
                    .add(MobPotionConfig.from(Effects.SPEED, 1, 10, 15))
                    .add(MobPotionConfig.from(Effects.SPEED, 2, 10, 50))
                    .add(MobPotionConfig.from(Effects.STRENGTH, 1, 10, 30))
                    .add(MobPotionConfig.from(Effects.FIRE_RESISTANCE, 1, 10, 10))
                    .add(MobPotionConfig.from(Effects.INVISIBILITY, 1, 10, 35))
                    .add(MobPotionConfig.from(Effects.RESISTANCE, 1, 10, 40))
                    .build());

            // Blights
            mobsExempt = wrapper
                    .builder("mob.blight.exemptedMobs")
                    .comment("These mobs won't be able to become blights")
                    .defineList(ImmutableList.of("Bat", "Cat", "Chicken"), ConfigValue.IS_NONEMPTY_STRING);
            blightPotions = MobPotionConfig.init(wrapper, "mob.blight.potionEffects", false, ImmutableList.<CommentedConfig>builder()
                    .add(MobPotionConfig.from(Effects.FIRE_RESISTANCE, 1, 5, 0))
                    .add(MobPotionConfig.from(Effects.RESISTANCE, 1, 5, 0))
                    .add(MobPotionConfig.from(Effects.SPEED, 4, 5, 0))
                    .add(MobPotionConfig.from(Effects.STRENGTH, 2, 5, 0))
                    .build());
        }

        public boolean isMobExempt(MobEntity entity) {
            String name = entity.getName().getString();
            List<? extends String> list = mobsExempt.get();
            for (String value : list) {
                if (value.equalsIgnoreCase(name)) {
                    ScalingHealth.LOGGER.debug("Stopped {} from becoming blight", name);
                    return true;
                }
            }
            return list.contains(name);
        }
    }

    public static class DamageScaling {
        public final DoubleValue difficultyWeight;
        public final DoubleValue genericScale;
        public final BooleanValue affectPlayers;
        public final BooleanValue affectHostiles;
        public final BooleanValue affectPeacefuls;
        public final EnumValue<net.silentchaos512.scalinghealth.event.DamageScaling.Mode> mode;
        private final ConfigSpec scalesSpec = new ConfigSpec();
        private final ConfigValue<List<? extends CommentedConfig>> scales;

        DamageScaling(ConfigSpecWrapper wrapper) {
            wrapper.comment("damageScaling",
                    "Set damage scaling by damage source. No scaling is done by default.",
                    "Mod sources can be added too, you'll just need the damage type string. The number represents how steeply the damage scales.",
                    "0 means no scaling (vanilla), 1 means it will be proportional to difficulty/max health (whichever you set).",
                    "The scale can be any non-negative number, including numbers greater than one.");

            difficultyWeight = wrapper
                    .builder("damageScaling.difficultyWeight")
                    .comment("How sharply damage scales with difficulty")
                    .defineInRange(0.04, 0, Double.MAX_VALUE);
            genericScale = wrapper
                    .builder("damageScaling.genericScale")
                    .comment("Scale for all damage types which does not have a specific scale defined.",
                            "This can have unintended side effects, so it's recommended to leave this at 0.")
                    .defineInRange(0.0, 0, Double.MAX_VALUE);
            affectPlayers = wrapper
                    .builder("damageScaling.affectPlayers")
                    .comment("Does damage scaling affect players?")
                    .define(true);
            affectHostiles = wrapper
                    .builder("damageScaling.affectHostiles")
                    .comment("Does damage scaling affect hostile mobs?")
                    .define(true);
            affectPeacefuls = wrapper
                    .builder("damageScaling.affectPeacefuls")
                    .comment("Does damage scaling affect peaceful mobs (animals)?")
                    .define(false);
            mode = wrapper
                    .builder("damageScaling.mode")
                    .comment("Damage scaling mode",
                            EnumValue.allValuesComment(net.silentchaos512.scalinghealth.event.DamageScaling.Mode.class))
                    .defineEnum(net.silentchaos512.scalinghealth.event.DamageScaling.Mode.MAX_HEALTH);

            scalesSpec.defineList("types", ImmutableList.of("cactus"), ConfigValue.IS_NONEMPTY_STRING);
            scalesSpec.define("scale", 0.0, o -> o instanceof Number && ((Number) o).doubleValue() >= 0);
            CommentedConfig scalesDefault = CommentedConfig.inMemory();
            scalesSpec.correct(scalesDefault);
            scales = wrapper
                    .builder("damageScaling.damageTypes")
                    .defineList(ImmutableList.of(scalesDefault), o -> {
                        if (!(o instanceof CommentedConfig)) return false;
                        return scalesSpec.isCorrect((CommentedConfig) o);
                    });
        }

        public double getScale(String damageType) {
            return scales.get().stream()
                    .filter(c -> c.<List<? extends String>>get("types").contains(damageType))
                    .findFirst()
                    .map(c -> c.<Double>get("scale"))
                    .orElseGet(genericScale::get);
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
        public final StringValue sleepWarningMessage;

        // Mutators
        public final Supplier<Expression> onBlightKilled;
        public final Supplier<Expression> onBossKilled;
        public final Supplier<Expression> onHostileKilled;
        public final Supplier<Expression> onPeacefulKilled;
        public final Supplier<Expression> onPlayerKilled;
        public final Supplier<Expression> onPlayerDeath;
        public final Supplier<Expression> onPlayerSleep;
        private final ConfigValue<List<? extends CommentedConfig>> byEntityMutators;

        // Multipliers
        private final ConfigValue<List<? extends CommentedConfig>> locationMultipliers;
        public final BooleanValue lunarCyclesEnabled;
        public final ConfigValue<List<? extends Double>> lunarCycleMultipliers;

        private final ConfigSpec byEntitySpec = new ConfigSpec();
        private final ConfigSpec locationMultipliersSpec = new ConfigSpec();

        Difficulty(ConfigSpecWrapper wrapper) {
            byEntitySpec.defineList("types", ImmutableList.of("minecraft:villager"), DimensionConfig::validateId);
            byEntitySpec.define("onKilled", "difficulty + 0.01", o -> validateExpression(o, "onKilled", EvalVars.PLAYER_DIFFICULTY));

            locationMultipliersSpec.defineList("biomes", ImmutableList.of("modid:example"), ConfigValue.IS_NONEMPTY_STRING);
            locationMultipliersSpec.defineList("dimensions", ImmutableList.of("overworld"), ConfigValue.IS_NONEMPTY_STRING);
            locationMultipliersSpec.define("scale", 1.0, o -> o instanceof Number && ((Number) o).doubleValue() >= 0);

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
                            "WEIGHTED_AVERAGE:",
                            "weighted average of players difficulty based on distance - players further away from an area will have less of an impact",
                            "AVERAGE:",
                            "Takes the average difficulty of nearby players",
                            "MIN_LEVEL:",
                            "Lowest difficulty of all nearby players",
                            "MAX_LEVEL:",
                            "Highest difficulty of all nearby players",
                            "DISTANCE_FROM_SPAWN:",
                            "Difficulty increases further away from world spawn, see distanceFactor config above.",
                            "DISTANCE_FROM_ORIGIN:",
                            "Same as DISTANCE_FROM_SPAWN but from (0, 0, 0)",
                            "DISTANCE_AND_TIME:",
                            "Mix of WEIGHTED_AVERAGE and DISTANCE_FROM_SPAWN. Difficulty increases with time but also with distance",
                            "DIMENSION_WIDE:",
                            "Difficulty tracked at the server level. Player difficulty is irrelevant.")
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
            sleepWarningMessage = wrapper
                    .builder("difficulty.sleepWarningMessage")
                    .comment("Message displayed to the player when sleeping, assuming it would change their difficulty.",
                            "If left empty, the default message is pulled from the translation file.")
                    .defineString("");

            // Mutators
            wrapper.comment("difficulty.mutators",
                    "Change difficulty when certain things happen",
                    "putting in -difficulty- produces no change, putting 0 will reset difficulty after this action");

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

            CommentedConfig byEntityDefault = CommentedConfig.inMemory();
            byEntitySpec.correct(byEntityDefault);
            CommentedConfig multipliersDefault = CommentedConfig.inMemory();
            locationMultipliersSpec.correct(multipliersDefault);

            // byEntity table list
            byEntityMutators = wrapper
                    .builder("difficulty.mutators.byEntity")
                    .defineList(ImmutableList.of(byEntityDefault), o -> {
                        if (!(o instanceof CommentedConfig)) return false;
                        return byEntitySpec.isCorrect((CommentedConfig) o);
                    });

            // Multipliers
            wrapper.comment("difficulty.multipliers.byLocation",
                    "Set difficulty multipliers based on location. You can match dimensions, biomes, or both.",
                    "If either the biomes or dimensions array is empty, it is ignored (matching all)");
            locationMultipliers = wrapper
                    .builder("difficulty.multipliers.byLocation")
                    .defineList(ImmutableList.of(multipliersDefault), o -> {
                        if (!(o instanceof CommentedConfig)) return false;
                        return locationMultipliersSpec.isCorrect((CommentedConfig) o);
                    });
            wrapper.comment("difficulty.multipliers.lunarCycles",
                    "Allows difficulty to be changed based on the moon phase. values must have 8 elements.",
                    "The first is full moon, the fifth is new moon.",
                    "'enabled' must be set to true for this to work.");
            lunarCyclesEnabled = wrapper
                    .builder("difficulty.multipliers.lunarCycles.enabled")
                    .define(false);
            lunarCycleMultipliers = wrapper
                    .builder("difficulty.multipliers.lunarCycles.values")
                    .defineList(ImmutableList.of(1.5, 1.3, 1.2, 1.0, 0.8, 1.0, 1.2, 1.3), o -> o instanceof Number);
        }

        /**
         * Check if player is difficulty exempt. Checks name (ignoring case) and UUID.
         *
         * @return True if exempt, false otherwise.
         */
        public boolean isExempt(PlayerEntity player) {
            List<? extends String> list = difficultyExempt.get();
            for (String value : list) {
                if (value.equalsIgnoreCase(player.getName().getFormattedText())) {
                    return true;
                }
            }
            return list.contains(player.getUniqueID().toString());
        }

        public Expression getKillMutator(LivingEntity entity) {
            // Might be nice if there was a more generic way to handle lists of tables.
            // But this works for now.
            String name = Objects.requireNonNull(entity.getType().getRegistryName()).toString();
            return byEntityMutators.get().stream()
                    .filter(c -> c.<List<? extends String>>get("types").contains(name))
                    .findFirst()
                    .map(c -> new Expression(c.get("onKilled")))
                    .orElseGet(() -> getDefaultKillMutator(entity));
        }

        public double getLocationMultiplier(IWorldReader world, BlockPos pos) {
            DimensionType type = world.getDimension().getType();
            Biome biome = world.getBiome(pos);
            //noinspection OverlyLongLambda
            return locationMultipliers.get().stream()
                    .filter(c -> {
                        List<? extends String> dimensions = c.get("dimensions");
                        List<? extends String> biomes = c.get("biomes");
                        if (dimensions.isEmpty() && biomes.isEmpty()) {
                            return false;
                        }
                        return DimensionUtils.containedInList(type, dimensions, true) && BiomeUtils.containedInList(biome, biomes, true);
                    })
                    .findFirst()
                    .map(c -> c.<Double>get("scale"))
                    .orElse(1.0);
        }

        @SuppressWarnings("ChainOfInstanceofChecks")
        public Expression getDefaultKillMutator(LivingEntity entity) {
            if (!entity.isNonBoss())
                return onBossKilled.get();
            if (entity instanceof MobEntity && BlightHandler.isBlight((MobEntity) entity))
                return onBlightKilled.get();
            if (entity instanceof IMob)
                return onHostileKilled.get();
            if (entity instanceof AnimalEntity)
                return onPeacefulKilled.get();
            if (entity instanceof PlayerEntity)
                return onPlayerKilled.get();
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

    private static boolean validateId(Object obj) {
        return obj instanceof String && ResourceLocation.tryCreate((String) obj) != null;
    }

    private final ConfigSpecWrapper wrapper;

    public final General general;
    public final Items item;
    public final Mobs mobs;
    public final Player player;
    public final Pets pets;
    public final Difficulty difficulty;
    public final DamageScaling damageScaling;

    private final String configFileName;
    private final int dimensionId;

    @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
    DimensionConfig(final Path path) {
        wrapper = ConfigSpecWrapper.create(path);
        general = new General(wrapper);
        item = new Items(wrapper);
        mobs = new Mobs(wrapper);
        player = new Player(wrapper);
        pets = new Pets(wrapper);
        difficulty = new Difficulty(wrapper);
        damageScaling = new DamageScaling(wrapper);

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
