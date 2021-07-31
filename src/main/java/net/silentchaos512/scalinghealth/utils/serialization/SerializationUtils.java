package net.silentchaos512.scalinghealth.utils.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.udojava.evalex.Expression;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.silentchaos512.scalinghealth.config.EvalVars;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SerializationUtils {
    public static final Codec<Supplier<Expression>> EXPRESSION_CODEC = Codec.STRING.comapFlatMap(s ->
            {
                if(s.isEmpty())
                    return DataResult.error("Empty Expression");

                Expression dummy = EvalVars.dummyPopulate(new Expression(s));
                try {
                    dummy.eval();
                } catch (Exception e) {
                    return DataResult.error("Could not parse Expression: " + e);
                }
                return DataResult.success(() -> new Expression(s));
            }, e -> e.get().getExpression());

    public static final Codec<AttributeModifier.Operation> ATTRIBUTE_OPERATION_CODEC = Codec.STRING
            .comapFlatMap(s -> {
                try {
                    AttributeModifier.Operation op = AttributeModifier.Operation.valueOf(s.toUpperCase(Locale.ROOT));
                    return DataResult.success(op);
                } catch (Exception e) {
                    return DataResult.error("No Operation named: " + s + ". Valid values are :" +
                            Arrays.stream(AttributeModifier.Operation.values())
                                    .map(Enum::name)
                                    .collect(Collectors.joining(", ")));
                }
            }, AttributeModifier.Operation::name);

    public static Codec<Integer> positiveInt() {
        return positiveInt(0);
    }

    public static Codec<Double> positiveDouble() {
        return positiveDouble(0);
    }

    public static Codec<Integer> positiveInt(int min) {
        if (min < 0)
            throw new RuntimeException("Use Codec#intRange instead");
        Function<Integer, DataResult<Integer>> bound = i ->
                i >= min ? DataResult.success(i) :
                        DataResult.error(i < 0 ? "Int " + i + " must be positive!" : "Value " + i + " is out of bounds");
        return Codec.INT.flatXmap(bound, bound);
    }

    public static Codec<Double> positiveDouble(double min) {
        if (min < 0)
            throw new RuntimeException("Use Codec#doubleRange instead");
        Function<Double, DataResult<Double>> bound = d ->
                d >= min ? DataResult.success(d) :
                        DataResult.error("Double " + d + " must be greater than " + min);
        return Codec.DOUBLE.flatXmap(bound, bound);
    }

    public static <N1 extends Number, N2 extends Number, N3 extends Number> MapCodec<NumberConstraint<N1, N2, N3>> numberConstraintCodec(
            Codec<N1> n1C, String n1Field,
            Codec<N2> n2C, String n2Field,
            Codec<N3> n3C, String n3Field
    ) {
        return RecordCodecBuilder.<NumberConstraint<N1, N2, N3>>mapCodec(inst ->
                inst.group(
                        n1C.fieldOf(n1Field).forGetter(i -> i.starting),
                        n2C.fieldOf(n2Field).forGetter(i -> i.min),
                        n3C.fieldOf(n3Field).forGetter(i -> i.max)
                ).apply(inst, NumberConstraint::new)
        ).flatXmap(NumberConstraint::verify, NumberConstraint::verify);
    }

    public static class NumberConstraint<N1 extends Number, N2 extends Number, N3 extends Number> {
        public final N1 starting;
        public final N2 min;
        public final N3 max;

        public NumberConstraint(N1 startingHp, N2 minHealth, N3 maxHealth) {
            this.starting = startingHp;
            this.min = minHealth;
            this.max = maxHealth;
        }

        private DataResult<NumberConstraint<N1, N2, N3>> verify() {
            if (this.min.doubleValue() > this.starting.doubleValue())
                return DataResult.error("Starting value can't be smaller than minimum value!");

            if (this.max.doubleValue() != 0) { //if max is 0, consider it is infinity.
                if (this.min.doubleValue() > this.max.doubleValue())
                    return DataResult.error("Minimum value  can't be greater than maximum value!");
                if (this.starting.doubleValue() > this.max.doubleValue())
                    return DataResult.error("Starting value can't be greater than maximum value!");
            }

            return DataResult.success(this);
        }
    }
}
