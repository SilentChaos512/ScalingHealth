package net.silentchaos512.scalinghealth.resources.mechanics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.udojava.evalex.Expression;
import net.silentchaos512.scalinghealth.utils.serialization.SerializationUtils;

import java.util.function.Supplier;

public class PlayerMechanics {
    public static final String FILE = "player";

    public static final Codec<PlayerMechanics> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    SerializationUtils.numberConstraintCodec(
                            SerializationUtils.positiveInt(1), "startingHealth",
                            SerializationUtils.positiveInt(1), "minHealth",
                            SerializationUtils.positiveInt(), "maxHealth"
                    ).forGetter(p -> new SerializationUtils.NumberConstraint<>(p.startingHp, p.minHealth, p.maxHealth)),
                    SerializationUtils.EXPRESSION_CODEC.fieldOf("setOnDeath").forGetter(p -> p.healthOnDeath),
                    SerializationUtils.positiveDouble().fieldOf("maxAttackDamage").forGetter(p -> p.maxAttackDamage),
                    SerializationUtils.positiveInt(1).fieldOf("levelsPerHp").forGetter(p -> p.startingHp),
                    SerializationUtils.positiveInt(1).fieldOf("hpPerLevel").forGetter(p -> p.startingHp),
                    RegenMechanics.CODEC.fieldOf("regen").forGetter(p -> p.regenMechanics)
            ).apply(inst, PlayerMechanics::new)
    );

    public final int startingHp;
    public final int minHealth;
    public final int maxHealth;
    public final Supplier<Expression> healthOnDeath;
    public final double maxAttackDamage;
    public final int levelsPerHp;
    public final int hpPerLevel;
    public final RegenMechanics regenMechanics;

    private PlayerMechanics(SerializationUtils.NumberConstraint<Integer, Integer, Integer> nc, Supplier<Expression> healthOnDeath, double maxAttackDamage, int levelsPerHp, int hpPerLevel, RegenMechanics regenMechanics) {
        this.startingHp = nc.starting;
        this.minHealth = nc.min;
        this.maxHealth = nc.max == 0 ? Integer.MAX_VALUE : nc.max;
        this.healthOnDeath = healthOnDeath;
        this.maxAttackDamage = maxAttackDamage;
        this.levelsPerHp = levelsPerHp;
        this.hpPerLevel = hpPerLevel;
        this.regenMechanics = regenMechanics;
    }

    public static class RegenMechanics {
        private static final Codec<RegenMechanics> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        SerializationUtils.positiveDouble().fieldOf("initialDelay").forGetter(r -> r.initialDelay),
                        SerializationUtils.positiveDouble().fieldOf("tickDelay").forGetter(r -> r.tickDelay),
                        SerializationUtils.positiveDouble().fieldOf("exhaustion").forGetter(r -> r.exhaustion),
                        Codec.BOOL.fieldOf("proportionalToMaxHealth").forGetter(r -> r.proportionaltoMaxHp),
                        SerializationUtils.positiveInt().fieldOf("minFood").forGetter(r -> r.minFood),
                        SerializationUtils.positiveInt().fieldOf("maxFood").forGetter(r -> r.maxFood),
                        SerializationUtils.positiveInt().fieldOf("minHealth").forGetter(r -> r.regenMinHealth),
                        SerializationUtils.positiveInt().fieldOf("maxHealth").forGetter(r -> r.regenMaxHealth)
                ).apply(inst, RegenMechanics::new)
        );

        public final double initialDelay;
        public final double tickDelay;
        public final double exhaustion;
        public final boolean proportionaltoMaxHp;
        public final int minFood;
        public final int maxFood;
        public final int regenMinHealth;
        public final int regenMaxHealth;

        private RegenMechanics(double initialDelay, double tickDelay, double exhaustion, boolean proportionaltoMaxHp, int minFood, int maxFood, int regenMinHealth, int regenMaxHealth) {
            this.initialDelay = initialDelay;
            this.tickDelay = tickDelay;
            this.exhaustion = exhaustion;
            this.proportionaltoMaxHp = proportionaltoMaxHp;
            this.minFood = minFood;
            this.maxFood = maxFood;
            this.regenMinHealth = regenMinHealth;
            this.regenMaxHealth = regenMaxHealth;
        }
    }
}
