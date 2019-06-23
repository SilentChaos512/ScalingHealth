package net.silentchaos512.scalinghealth.config;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.silentchaos512.utils.config.BooleanValue;
import net.silentchaos512.utils.config.ConfigSpecWrapper;
import net.silentchaos512.utils.config.DoubleValue;
import net.silentchaos512.utils.config.IntValue;

public class RegenConfig {
    private BooleanValue enabled;
    private DoubleValue initialDelay;
    private DoubleValue tickDelay;
    private DoubleValue exhaustion;
    private BooleanValue proportionalToMaxHealth;
    private IntValue minFood;
    private IntValue maxFood;
    private IntValue minHealth;
    private IntValue maxHealth;

    public static RegenConfig init(ConfigSpecWrapper wrapper, String path, boolean addComment) {
        if (addComment) {
            wrapper.comment(path, "Regen config",
                    "enabled -- Quick toggle for regen",
                    "initialDelay -- Time in seconds after taking damage before regen begins",
                    "tickDelay -- Time in seconds between healing ticks",
                    "exhaustion -- Food consumed per healing tick",
                    "proportionalToMaxHealth -- If true, healing scales with max health. Otherwise, heals 1 health per healing tick",
                    "minFood -- Minimum food required for regen to be active",
                    "maxFood -- Maximum food at which regen will be active",
                    "minHealth -- Minimum health required for regen to be active",
                    "maxHealth -- Maximum health at which regen will be active");
        }

        RegenConfig config = new RegenConfig();
        config.enabled = wrapper
                .builder(path + ".enabled")
                .define(true);
        config.initialDelay = wrapper
                .builder(path + ".initialDelay")
                .defineInRange(20.0, 0, Double.MAX_VALUE);
        config.tickDelay = wrapper
                .builder(path + ".tickDelay")
                .defineInRange(5.0, 0, Double.MAX_VALUE);
        config.exhaustion = wrapper
                .builder(path + ".exhaustion")
                .defineInRange(0.1, 0, Double.MAX_VALUE);
        config.minFood = wrapper
                .builder(path + ".minFood")
                .defineInRange(10, 0, Integer.MAX_VALUE);
        config.maxFood = wrapper
                .builder(path + ".maxFood")
                .defineInRange(Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        config.minHealth = wrapper
                .builder(path + ".minHealth")
                .defineInRange(0, 0, Integer.MAX_VALUE);
        config.maxHealth = wrapper
                .builder(path + ".maxHealth")
                .defineInRange(Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        config.proportionalToMaxHealth = wrapper
                .builder(path + ".proportionalToMaxHealth")
                .define(false);
        return config;
    }

    public boolean isActive(LivingEntity entity) {
        if (!this.enabled.get() || !entity.isAlive() || entity.getHealth() >= entity.getMaxHealth()) {
            return false;
        }

        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            int food = player.getFoodStats().getFoodLevel();
            if (food < this.minFood.get() || food > this.maxFood.get()) {
                return false;
            }
        }

        float health = entity.getHealth();
        return health >= this.minHealth.get() && health <= this.maxHealth.get();
    }

    public float getHealTickAmount(LivingEntity entity) {
        if (this.proportionalToMaxHealth.get()) {
            IAttributeInstance attr = entity.getAttribute(SharedMonsterAttributes.MAX_HEALTH);
            double base = attr.getBaseValue();
            double max = attr.getValue();
            return (float) (max / base);
        }
        return 1;
    }

    public int getInitialDelay() {
        return (int) (20 * this.initialDelay.get());
    }

    public int getTickDelay() {
        return (int) (20 * this.tickDelay.get());
    }

    public float getExhaustion() {
        return this.exhaustion.get().floatValue();
    }
}
