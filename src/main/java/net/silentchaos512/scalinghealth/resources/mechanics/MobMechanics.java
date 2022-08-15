package net.silentchaos512.scalinghealth.resources.mechanics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.silentchaos512.scalinghealth.utils.mode.MobHealthMode;
import net.silentchaos512.scalinghealth.utils.serialization.DifficultyMobEffect;
import net.silentchaos512.scalinghealth.utils.serialization.SerializationUtils;

import java.util.Collections;
import java.util.List;

public record MobMechanics(MobHealthMode mode,
                           Generic generic,
                           List<DifficultyMobEffect> mobEffects,
                           Blight blight,
                           Pets pets) {
    public static final String FILE = "mobs";

    public static final MobMechanics DEFAULT = new MobMechanics(
            new MobHealthMode(AttributeModifier.Operation.MULTIPLY_BASE, 0.5),
            Generic.DEFAULT,
            Collections.EMPTY_LIST,
            Blight.DEFAULT,
            Pets.DEFAULT
    );

    public static final Codec<MobMechanics> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    MobHealthMode.CODEC.fieldOf("healthMode").forGetter(m -> m.mode),
                    Generic.CODEC.fieldOf("general").forGetter(m -> m.generic),
                    DifficultyMobEffect.CODEC.listOf().fieldOf("effects").forGetter(m -> m.mobEffects),
                    Blight.CODEC.fieldOf("blights").forGetter(m -> m.blight),
                    Pets.CODEC.fieldOf("pets").forGetter(m -> m.pets)
            ).apply(inst, MobMechanics::new)
    );

    public record Generic(double passiveMultiplier, double hostileMultiplier, double hostilePotionChance,
                          double peacefulPotionChance, double damageBoostScale, double maxDamageBoost,
                          double spawnerModifier, double xpBoost) {

        public static final Generic DEFAULT = new Generic(
                0.375,
                0.375,
                0.06,
                0.005,
                0.1,
                10,
                0.3,
                0.03
        );

        public static final Codec<Generic> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        SerializationUtils.positiveDouble().fieldOf("passiveMultiplier").forGetter(g -> g.passiveMultiplier),
                        SerializationUtils.positiveDouble().fieldOf("hostileMultiplier").forGetter(g -> g.hostileMultiplier),
                        SerializationUtils.positiveDouble().fieldOf("hostilePotionChance").forGetter(g -> g.hostilePotionChance),
                        SerializationUtils.positiveDouble().fieldOf("peacefulPotionChance").forGetter(g -> g.peacefulPotionChance),
                        SerializationUtils.positiveDouble().fieldOf("damageBoostScale").forGetter(g -> g.damageBoostScale),
                        SerializationUtils.positiveDouble().fieldOf("maxDamageBoost").forGetter(g -> g.maxDamageBoost),
                        SerializationUtils.positiveDouble().fieldOf("spawnerModifier").forGetter(g -> g.spawnerModifier),
                        SerializationUtils.positiveDouble().fieldOf("xpBoost").forGetter(g -> g.xpBoost)
                ).apply(inst, Generic::new)
        );
    }

    public record Blight(double blightChance, boolean notifyBlightDeath, double blightXpBoost,
                         double blightDifficultyModifier,
                         List<DifficultyMobEffect> blightEffects) {

        public static final Blight DEFAULT = new Blight(
                0.03,
                true,
                2,
                1.7,
                Collections.EMPTY_LIST
        );

        public static final Codec<Blight> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        SerializationUtils.positiveDouble().fieldOf("blightChance").forGetter(b -> b.blightChance),
                        Codec.BOOL.fieldOf("notifyBlightDeath").forGetter(b -> b.notifyBlightDeath),
                        SerializationUtils.positiveDouble().fieldOf("blightXpBoost").forGetter(b -> b.blightXpBoost),
                        SerializationUtils.positiveDouble().fieldOf("blightDifficultyModifier").forGetter(b -> b.blightDifficultyModifier),
                        DifficultyMobEffect.CODEC.listOf().fieldOf("effects").forGetter(b -> b.blightEffects)
                ).apply(inst, Blight::new)
        );

    }

    public record Pets(int petsRegenDelay, int petsHealthCrystalGain) {
        public static final Pets DEFAULT = new Pets(
                30, 5
        );

        public static final Codec<Pets> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        SerializationUtils.positiveInt().fieldOf("petsRegenDelay").forGetter(p -> p.petsRegenDelay),
                        SerializationUtils.positiveInt().fieldOf("petsHealthCrystalGain").forGetter(p -> p.petsHealthCrystalGain)
                ).apply(inst, Pets::new)
        );
    }
}
