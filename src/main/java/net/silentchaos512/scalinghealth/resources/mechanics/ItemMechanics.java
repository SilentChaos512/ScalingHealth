package net.silentchaos512.scalinghealth.resources.mechanics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.silentchaos512.scalinghealth.utils.serialization.SerializationUtils;

public record ItemMechanics(double cursedHeartChange, double enchantedHeartChange, int chanceHeartChange,
                            double heartCrystalBonusRegen, int heartCrystalLevelCost, int heartCrystalHealthIncrease,
                            int powerCrystalLevelCost, double powerCrystalDamageIncrease) {
    public static final String FILE = "items";

    public static final ItemMechanics DEFAULT = new ItemMechanics(
            10,
            -10,
            10,
            4,
            3,
            1,
            3,
            0.5
    );

    public static final Codec<ItemMechanics> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    Codec.DOUBLE.fieldOf("cursedHeartChange").forGetter(i -> i.cursedHeartChange),
                    Codec.DOUBLE.fieldOf("enchantedHeartChange").forGetter(i -> i.enchantedHeartChange),
                    Codec.INT.fieldOf("chanceHeartChange").forGetter(i -> i.chanceHeartChange),
                    SerializationUtils.positiveDouble().fieldOf("heartCrystalBonusRegen").forGetter(i -> i.heartCrystalBonusRegen),
                    SerializationUtils.positiveInt().fieldOf("heartCrystalLevelCost").forGetter(i -> i.heartCrystalLevelCost),
                    SerializationUtils.positiveInt().fieldOf("heartCrystalHealthIncrease").forGetter(i -> i.heartCrystalHealthIncrease),
                    SerializationUtils.positiveInt().fieldOf("powerCrystalLevelCost").forGetter(i -> i.powerCrystalLevelCost),
                    SerializationUtils.positiveDouble().fieldOf("powerCrystalDamageIncrease").forGetter(i -> i.powerCrystalDamageIncrease)
            ).apply(inst, ItemMechanics::new)
    );
}
