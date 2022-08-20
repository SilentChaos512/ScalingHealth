package net.silentchaos512.scalinghealth.utils.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.SHConfig;
import net.silentchaos512.scalinghealth.utils.EntityGroup;
import net.silentchaos512.utils.MathUtils;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public record DifficultyMobEffect(MobEffect effect, int level, int minDifficulty,
                                  double durationMinutes) {
    private static final Marker MARKER = MarkerManager.getMarker("DifficultyMobEffects");

    public static final Codec<DifficultyMobEffect> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    ResourceLocation.CODEC.comapFlatMap(
                            rl -> ForgeRegistries.MOB_EFFECTS.containsKey(rl) ?
                                    DataResult.success(ForgeRegistries.MOB_EFFECTS.getValue(rl)) :
                                    DataResult.error("No potion named:" + rl.toString()),
                            ForgeRegistries.MOB_EFFECTS::getKey
                    ).fieldOf("effect").forGetter(e -> e.effect),
                    SerializationUtils.positiveInt().fieldOf("level").forGetter(e -> e.level),
                    SerializationUtils.positiveInt().fieldOf("minDifficulty").forGetter(e -> e.minDifficulty),
                    SerializationUtils.positiveDouble().fieldOf("durationInMinutes").forGetter(e -> e.durationMinutes)
            ).apply(inst, DifficultyMobEffect::new)
    );

    public void apply(LivingEntity e, double difficulty) {
        if (difficulty >= minDifficulty) {
            e.addEffect(new MobEffectInstance(effect, (int) (durationMinutes * 60 * 20), level - 1));
            if (ScalingHealth.LOGGER.isDebugEnabled() && SHConfig.SERVER.debugMobPotionEffects.get()) {
                ScalingHealth.LOGGER.debug(MARKER, "Applied effect {}, level {} for {}min to {} ({})",
                        ForgeRegistries.MOB_EFFECTS.getKey(effect), level, durationMinutes, e.getScoreboardName(), ForgeRegistries.ENTITY_TYPES.getKey(e.getType()));
            }
        }
    }

    public void tryApply(LivingEntity e, double difficulty) {
        if (MathUtils.tryPercentage(EntityGroup.from(e).getPotionChance()))
            apply(e, difficulty);
    }
}
