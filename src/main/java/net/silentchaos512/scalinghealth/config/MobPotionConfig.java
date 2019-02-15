package net.silentchaos512.scalinghealth.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigSpec;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.lib.util.TimeUtils;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.lib.MobType;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MobPotionConfig {
    private static final Marker MARKER = MarkerManager.getMarker("MobPotionConfig");
    private Lazy<List<PotionEntry>> potions;
    private final List<PotionEntry> temp = new ArrayList<>();

    public void tryApply(EntityLivingBase entity, double difficulty) {
        double chance = MobType.from(entity).getPotionChance(entity);
        if (!MathUtils.tryPercentage(chance)) return;

        debug(() -> "tryApply " + entity);
        temp.clear();
        for (PotionEntry entry : potions.get()) {
            if (entry.cost < difficulty) {
                temp.add(entry);
            }
        }
        if (!temp.isEmpty()) {
            PotionEntry entry = temp.get(MathUtils.nextInt(temp.size()));
            debug(() -> "selected " + entry + " from " + temp.size() + " entries");
            entry.applyTo(entity);
        }
    }

    private static void debug(Supplier<?> message) {
        if (ScalingHealth.LOGGER.isDebugEnabled()) {
            ScalingHealth.LOGGER.debug(MARKER, message.get());
        }
    }

    public static MobPotionConfig init(ConfigSpecWrapper wrapper, String path) {
        ConfigSpec spec = new ConfigSpec();
        spec.define("potion", "minecraft:unknown", ConfigValue.IS_NONEMPTY_STRING);
        spec.defineInRange("level", 1, 1, 10);
        spec.defineInRange("minDifficulty", 0, 0, Integer.MAX_VALUE);
        spec.defineInRange("durationInMinutes", 10.0, 0.0, Double.MAX_VALUE);

        List<CommentedConfig> defaultSettings = new ArrayList<>();
        defaultSettings.add(from(MobEffects.SPEED, 1, 15));
        defaultSettings.add(from(MobEffects.SPEED, 2, 50));
        defaultSettings.add(from(MobEffects.STRENGTH, 1, 30));
        defaultSettings.add(from(MobEffects.FIRE_RESISTANCE, 1, 10));
        defaultSettings.add(from(MobEffects.INVISIBILITY, 1, 35));
        defaultSettings.add(from(MobEffects.RESISTANCE, 1, 40));

        ConfigValue<List<? extends CommentedConfig>> config = wrapper
                .builder(path)
                .defineList(defaultSettings, o -> {
                    if (!(o instanceof CommentedConfig)) return false;
                    return spec.isCorrect((CommentedConfig) o);
                });
        MobPotionConfig result = new MobPotionConfig();
        result.potions = Lazy.of(() -> config.get()
                .stream()
                .map(PotionEntry::from)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return result;
    }

    private static CommentedConfig from(Potion potion, int level, int cost) {
        CommentedConfig config = CommentedConfig.inMemory();
        config.set("potion", potion.getRegistryName().toString());
        config.set("level", level);
        config.set("minDifficulty", cost);
        config.set("durationInMinutes", 10.0);
        return config;
    }

    static class PotionEntry {
        final Potion potion;
        final int level;
        final int cost;
        final int duration;

        PotionEntry(Potion potion, int level, int cost, int duration) {
            this.potion = potion;
            this.level = level;
            this.cost = cost;
            this.duration = duration;
        }

        @Nullable
        static PotionEntry from(CommentedConfig config) {
            String nameRaw = config.get("potion");
            ResourceLocation name = ResourceLocation.makeResourceLocation(nameRaw);
            if (name == null) {
                ScalingHealth.LOGGER.error(MARKER, "Invalid ID {}", nameRaw);
                return null;
            }
            Potion potion = ForgeRegistries.POTIONS.getValue(name);
            if (potion == null) {
                ScalingHealth.LOGGER.error(MARKER, "No potion with ID {}", name);
                return null;
            }
            int level = config.getOrElse("level", 1);
            if (level < 1) return null;
            int cost = config.getOrElse("minDifficulty", 10);
            float durationInMinutes = config.getOrElse("durationInMinutes", 10.0).floatValue();
            int duration = TimeUtils.ticksFromMinutes(durationInMinutes);

            return new PotionEntry(potion, level, cost, duration);
        }

        void applyTo(EntityLivingBase entity) {
            entity.addPotionEffect(new PotionEffect(potion, duration, level - 1));
        }

        @Override
        public String toString() {
            return "PotionEntry{" +
                    "potion=" + potion.getRegistryName() +
                    ", level=" + level +
                    ", cost=" + cost +
                    ", duration=" + duration +
                    "}";
        }
    }
}
