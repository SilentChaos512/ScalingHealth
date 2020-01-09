package net.silentchaos512.scalinghealth.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigSpec;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.lib.util.TimeUtils;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.lib.EntityGroup;
import net.silentchaos512.utils.Lazy;
import net.silentchaos512.utils.MathUtils;
import net.silentchaos512.utils.config.ConfigSpecWrapper;
import net.silentchaos512.utils.config.ConfigValue;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MobPotionConfig {
    private static final Marker MARKER = MarkerManager.getMarker("MobPotionConfig");
    private Lazy<List<EffectEntry>> potions;
    private final List<EffectEntry> temp = new ArrayList<>();

    public void tryApply(LivingEntity entity, double difficulty) {
        double chance = EntityGroup.from(entity).getPotionChance(entity);
        if (!MathUtils.tryPercentage(chance)) return;

        temp.clear();
        for (EffectEntry entry : potions.get()) {
            if (entry.cost < difficulty) {
                temp.add(entry);
            }
        }
        if (!temp.isEmpty()) {
            EffectEntry entry = temp.get(MathUtils.nextInt(temp.size()));
            entry.applyTo(entity);
            if (ScalingHealth.LOGGER.isDebugEnabled()) {
                ScalingHealth.LOGGER.debug(MARKER, "Applied {} from {} effects to {}", entry, temp.size(), entity.getScoreboardName());
            }
        }
    }

    public void applyAll(LivingEntity entity) {
        this.potions.get().forEach(entry -> entry.applyTo(entity));
        if (ScalingHealth.LOGGER.isDebugEnabled()) {
            //ScalingHealth.LOGGER.debug(MARKER, "Applied all {} effects to {}", potions.get().size(), entity.getScoreboardName());
        }
    }

    public static MobPotionConfig init(ConfigSpecWrapper wrapper, String path, boolean includeCost, List<CommentedConfig> defaultSettings) {
        ConfigSpec spec = new ConfigSpec();
        spec.define("potion", "minecraft:unknown", ConfigValue.IS_NONEMPTY_STRING);
        spec.defineInRange("level", 1, 1, 10);
        spec.defineInRange("minDifficulty", 0, 0, Integer.MAX_VALUE);
        spec.defineInRange("durationInMinutes", 10.0, 0.0, Double.MAX_VALUE);

        ConfigValue<List<? extends CommentedConfig>> config = wrapper
                .builder(path)
                .defineList(defaultSettings, o -> {
                    if (!(o instanceof CommentedConfig)) return false;
                    return spec.isCorrect((CommentedConfig) o);
                });
        MobPotionConfig result = new MobPotionConfig();
        result.potions = Lazy.of(() -> config.get()
                .stream()
                .map(c -> EffectEntry.from(c, includeCost))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return result;
    }

    static CommentedConfig from(Effect potion, int level, double durationInMinutes, int cost) {
        CommentedConfig config = CommentedConfig.inMemory();
        config.set("potion", Objects.requireNonNull(potion.getRegistryName()).toString());
        config.set("level", level);
        config.set("minDifficulty", cost);
        config.set("durationInMinutes", durationInMinutes);
        return config;
    }

    static class EffectEntry {
        final Effect potion;
        final int level;
        final int cost;
        final int duration;
        // TODO: Include refresh boolean?

        EffectEntry(Effect potion, int level, int cost, int duration) {
            this.potion = potion;
            this.level = level;
            this.cost = cost;
            this.duration = duration;
        }

        @Nullable
        static EffectEntry from(CommentedConfig config, boolean includeCost) {
            String nameRaw = config.get("potion");
            ResourceLocation name = ResourceLocation.tryCreate(nameRaw);
            if (name == null) {
                ScalingHealth.LOGGER.error(MARKER, "Invalid ID {}", nameRaw);
                return null;
            }
            Effect potion = ForgeRegistries.POTIONS.getValue(name);
            if (potion == null) {
                ScalingHealth.LOGGER.error(MARKER, "No potion with ID {}", name);
                return null;
            }
            int level = config.getOrElse("level", 1);
            if (level < 1) return null;
            int cost = includeCost ? config.getOrElse("minDifficulty", 10) : 0;
            float durationInMinutes = config.getOrElse("durationInMinutes", 10.0).floatValue();
            int duration = TimeUtils.ticksFromMinutes(durationInMinutes);

            return new EffectEntry(potion, level, cost, duration);
        }

        void applyTo(LivingEntity entity) {
            entity.addPotionEffect(new EffectInstance(potion, duration, level - 1));
        }

        @Override
        public String toString() {
            return "EffectEntry{" +
                    "potion=" + potion.getRegistryName() +
                    ", level=" + level +
                    ", cost=" + cost +
                    ", duration=" + duration +
                    "}";
        }
    }
}
