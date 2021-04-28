package net.silentchaos512.scalinghealth.utils.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.SHConfig;
import net.silentchaos512.scalinghealth.utils.EntityGroup;
import net.silentchaos512.utils.MathUtils;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class DifficultyMobEffect {
    private static final Marker MARKER = MarkerManager.getMarker("DifficultyMobEffects");

    public static final Codec<DifficultyMobEffect> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    ResourceLocation.CODEC.comapFlatMap(
                            rl -> ForgeRegistries.POTIONS.containsKey(rl) ?
                                    DataResult.success(ForgeRegistries.POTIONS.getValue(rl)) :
                                    DataResult.error("No potion named:" + rl.toString()),
                            ForgeRegistryEntry::getRegistryName
                    ).fieldOf("effect").forGetter(e -> e.effect),
                    SerializationUtils.positiveInt().fieldOf("level").forGetter(e -> e.level),
                    SerializationUtils.positiveInt().fieldOf("minDifficulty").forGetter(e -> e.minDifficulty),
                    SerializationUtils.positiveDouble().fieldOf("durationInMinutes").forGetter(e -> e.durationMinutes)
            ).apply(inst, DifficultyMobEffect::new)
    );

    public final Effect effect;
    public final int level;
    public final int minDifficulty;
    public final double durationMinutes;

    public DifficultyMobEffect(Effect effect, int level, int minDifficulty, double durationMinutes) {
        this.effect = effect;
        this.level = level;
        this.minDifficulty = minDifficulty;
        this.durationMinutes = durationMinutes;
    }

    public void apply(LivingEntity e, double difficulty) {
        if (difficulty >= minDifficulty) {
            e.addPotionEffect(new EffectInstance(effect, (int) (durationMinutes * 60 * 20), level - 1));
            if (ScalingHealth.LOGGER.isDebugEnabled() && SHConfig.SERVER.debugMobPotionEffects.get()) {
                ScalingHealth.LOGGER.debug(MARKER, "Applied effect {}, level {} for {}min to {} ({})",
                        effect.getRegistryName(), level, durationMinutes, e.getScoreboardName(), e.getType().getRegistryName());
            }
        }
    }

    public void tryApply(LivingEntity e, double difficulty) {
        if (MathUtils.tryPercentage(EntityGroup.from(e).getPotionChance()))
            apply(e, difficulty);
    }
}
