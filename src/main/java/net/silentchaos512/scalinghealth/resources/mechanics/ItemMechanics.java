package net.silentchaos512.scalinghealth.resources.mechanics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.silentchaos512.scalinghealth.utils.serialization.SerializationUtils;

public class ItemMechanics {
    public static final String FILE = "items";

    public static final Codec<ItemMechanics> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    Codec.DOUBLE.fieldOf("cursedHeartChange").forGetter(i -> i.cursedHeartChange),
                    Codec.DOUBLE.fieldOf("enchantedHeartChange").forGetter(i -> i.enchantedHeartChange),
                    Codec.INT.fieldOf("chanceHeartChange").forGetter(i -> i.chanceHeartChange),
                    SerializationUtils.positiveDouble().fieldOf("heartCrystalBonusRegen").forGetter(i -> i.heartCrystalBonusRegen),
                    SerializationUtils.positiveInt().fieldOf("heartCrystalLevelCost").forGetter(i -> i.heartCrystalLevelCost),
                    SerializationUtils.positiveInt().fieldOf("heartCrystalHealthIncrease").forGetter(i -> i.heartCrystalHealthIncrease),
                    SerializationUtils.positiveInt().fieldOf("powerCrystalLevelCost").forGetter(i -> i.powerCrystalLevelCost),
                    SerializationUtils.positiveDouble().fieldOf("heartCrystalBonusRegen").forGetter(i -> i.heartCrystalBonusRegen)
            ).apply(inst, ItemMechanics::new)
    );

    public final double cursedHeartChange;
    public final double enchantedHeartChange;
    public final int chanceHeartChange;
    public final double heartCrystalBonusRegen;
    public final int heartCrystalLevelCost;
    public final int heartCrystalHealthIncrease;
    public final int powerCrystalLevelCost;
    public final double powerCrystalDamageIncrease;

    public ItemMechanics(double cursedHeartChange, double enchantedHeartChange, int chanceHeartChange, double heartCrystalBonusRegen, int heartCrystalLevelCost, int heartCrystalHealthIncrease, int powerCrystalLevelCost, double powerCrystalDamageIncrease) {
        this.cursedHeartChange = cursedHeartChange;
        this.enchantedHeartChange = enchantedHeartChange;
        this.chanceHeartChange = chanceHeartChange;
        this.heartCrystalBonusRegen = heartCrystalBonusRegen;
        this.heartCrystalLevelCost = heartCrystalLevelCost;
        this.heartCrystalHealthIncrease = heartCrystalHealthIncrease;
        this.powerCrystalLevelCost = powerCrystalLevelCost;
        this.powerCrystalDamageIncrease = powerCrystalDamageIncrease;
    }
}
