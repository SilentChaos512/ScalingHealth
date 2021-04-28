package net.silentchaos512.scalinghealth.resources.mechanics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.silentchaos512.scalinghealth.utils.mode.MobHealthMode;
import net.silentchaos512.scalinghealth.utils.serialization.DifficultyMobEffect;
import net.silentchaos512.scalinghealth.utils.serialization.SerializationUtils;

import java.util.List;

public class MobMechanics {
    public static final String FILE = "mobs";

    public static final Codec<MobMechanics> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    MobHealthMode.CODEC.fieldOf("healthMode").forGetter(m -> m.mode),
                    Generic.CODEC.fieldOf("general").forGetter(m -> m.generic),
                    DifficultyMobEffect.CODEC.listOf().fieldOf("effects").forGetter(m -> m.mobEffects),
                    Blight.CODEC.fieldOf("blights").forGetter(m -> m.blight),
                    Pets.CODEC.fieldOf("pets").forGetter(m -> m.pets)
            ).apply(inst, MobMechanics::new)
    );

    public final MobHealthMode mode;
    public final Generic generic;
    public final List<DifficultyMobEffect> mobEffects;
    public final Blight blight;
    public final Pets pets;

    public MobMechanics(MobHealthMode mode, Generic generic, List<DifficultyMobEffect> mobEffects, Blight blight, Pets pets) {
        this.mode = mode;
        this.generic = generic;
        this.mobEffects = mobEffects;
        this.blight = blight;
        this.pets = pets;
    }

    public static class Generic {
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

        public final double passiveMultiplier;
        public final double hostileMultiplier;
        public final double hostilePotionChance;
        public final double peacefulPotionChance;
        public final double damageBoostScale;
        public final double maxDamageBoost;
        public final double spawnerModifier;
        public final double xpBoost;

        public Generic(double passiveMultiplier, double hostileMultiplier, double hostilePotionChance, double peacefulPotionChance, double damageBoostScale, double maxDamageBoost, double spawnerModifier, double xpBoost) {
            this.passiveMultiplier = passiveMultiplier;
            this.hostileMultiplier = hostileMultiplier;
            this.hostilePotionChance = hostilePotionChance;
            this.peacefulPotionChance = peacefulPotionChance;
            this.damageBoostScale = damageBoostScale;
            this.maxDamageBoost = maxDamageBoost;
            this.spawnerModifier = spawnerModifier;
            this.xpBoost = xpBoost;
        }
    }

    public static class Blight {
        public static final Codec<Blight> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        SerializationUtils.positiveDouble().fieldOf("blightChance").forGetter(b -> b.blightChance),
                        Codec.BOOL.fieldOf("notifyBlightDeath").forGetter(b -> b.notifyBlightDeath),
                        SerializationUtils.positiveDouble().fieldOf("blightXpBoost").forGetter(b -> b.blightXpBoost),
                        SerializationUtils.positiveDouble().fieldOf("blightDifficultyModifier").forGetter(b -> b.blightDifficultyModifier),
                        DifficultyMobEffect.CODEC.listOf().fieldOf("effects").forGetter(b -> b.blightEffects)
                ).apply(inst, Blight::new)
        );

        public final double blightChance;
        public final boolean notifyBlightDeath;
        public final double blightXpBoost;
        public final double blightDifficultyModifier;
        public final List<DifficultyMobEffect> blightEffects;

        public Blight(double blightChance, boolean notifyBlightDeath, double blightXpBoost, double blightDifficultyModifier, List<DifficultyMobEffect> blightEffects) {
            this.blightChance = blightChance;
            this.notifyBlightDeath = notifyBlightDeath;
            this.blightXpBoost = blightXpBoost;
            this.blightDifficultyModifier = blightDifficultyModifier;
            this.blightEffects = blightEffects;
        }
    }

    public static class Pets {
        public static final Codec<Pets> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        SerializationUtils.positiveInt().fieldOf("petsRegenDelay").forGetter(p -> p.petsRegenDelay),
                        SerializationUtils.positiveInt().fieldOf("petsHealthCrystalGain").forGetter(p -> p.petsHealthCrystalGain)
                ).apply(inst, Pets::new)
        );

        public final int petsRegenDelay;
        public final int petsHealthCrystalGain;

        public Pets(int petsRegenDelay, int petsHealthCrystalGain) {
            this.petsRegenDelay = petsRegenDelay;
            this.petsHealthCrystalGain = petsHealthCrystalGain;
        }
    }
}
