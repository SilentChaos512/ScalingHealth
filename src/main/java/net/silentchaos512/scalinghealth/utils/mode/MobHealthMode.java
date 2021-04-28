package net.silentchaos512.scalinghealth.utils.mode;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.silentchaos512.scalinghealth.utils.serialization.SerializationUtils;

public class MobHealthMode {
    public static final Codec<MobHealthMode> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    SerializationUtils.ATTRIBUTE_OPERATION_CODEC.fieldOf("operation").forGetter(m -> m.op),
                    Codec.DOUBLE.optionalFieldOf("scaling_reduction", 0D).forGetter(m -> m.scaleReduction)
            ).apply(inst, MobHealthMode::new)
    );

    private final AttributeModifier.Operation op;
    private final double scaleReduction;

    public MobHealthMode(AttributeModifier.Operation op, double scaleReduction) {
        this.op = op;
        this.scaleReduction = scaleReduction;
    }

    public double getModifierHealth(double healthBoost, double baseMaxHp) {
        if (this.op == AttributeModifier.Operation.ADDITION)
            return healthBoost;

        double healthScale = this.scaleReduction * Math.max(0, baseMaxHp - 20);
        return healthBoost / (20 + healthScale);
    }

    public AttributeModifier.Operation getOp() {
        return this.op;
    }
}
