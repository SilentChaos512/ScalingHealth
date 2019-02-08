package net.silentchaos512.scalinghealth.difficulty;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultyAffected;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultySource;
import net.silentchaos512.scalinghealth.config.DimensionConfig;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public final class DifficultyEvents {
    public static final Marker MARKER = MarkerManager.getMarker("Difficulty");
    public static final String NBT_ENTITY_DIFFICULTY = ScalingHealth.RESOURCE_PREFIX + "difficulty";

    private DifficultyEvents() {}

    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (CapabilityDifficultyAffected.canAttachTo(entity)) {
//            ScalingHealth.LOGGER.debug(MARKER, event.getObject());
            event.addCapability(CapabilityDifficultyAffected.NAME, new CapabilityDifficultyAffected());
        }
        if (CapabilityDifficultySource.canAttachTo(entity)) {
            ScalingHealth.LOGGER.debug(MARKER, "Attaching difficulty source capability to {}", entity);
            event.addCapability(CapabilityDifficultySource.NAME, new CapabilityDifficultySource());
        }
    }

    @SubscribeEvent
    public static void onAttachWorldCababilities(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
        if (CapabilityDifficultySource.canAttachTo(world)) {
            event.addCapability(CapabilityDifficultySource.NAME, new CapabilityDifficultySource());
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) return;

        entity.getCapability(CapabilityDifficultyAffected.INSTANCE).ifPresent(iDifficultyAffected -> {
            if (iDifficultyAffected.getDifficulty() < 10) {
                ScalingHealth.LOGGER.debug(MARKER, "We found a {}! Difficulty={}", entity, iDifficultyAffected.getDifficulty());
                iDifficultyAffected.setDifficulty(iDifficultyAffected.getDifficulty() + 5);
            }
        });
        if (entity.world.getGameTime() % 20 == 0) {
            entity.getCapability(CapabilityDifficultySource.INSTANCE).ifPresent(source -> {
                source.setDifficulty(source.getDifficulty() + 0.001f);
                ScalingHealth.LOGGER.debug(MARKER, "Source {} difficulty is now {}", entity, source.getDifficulty());
            });
        }

//        DimensionConfig config = Config.get(entity);
//
//        if (config.difficulty.maxValue.get() <= 0 || !healthIncreaseAllowed(entity, config))
//            return;
//
//        applyDifficulty(entity, config);
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.getGameTime() % 20 == 0) {
            event.world.getCapability(CapabilityDifficultySource.INSTANCE).ifPresent(source -> {
                source.setDifficulty(source.getDifficulty() + 0.001f);
                ScalingHealth.LOGGER.debug(MARKER, "World {} difficulty is now {}", event.world, source.getDifficulty());
            });
        }
    }

    private static void applyDifficulty(EntityLivingBase entity, DimensionConfig config) {
//        World world = entity.world;
//        double difficulty = config.difficulty.areaMode.get().getAreaDifficulty(world, entity.getPosition());
//
//        final double originalDifficulty = difficulty;
//        final double originalMaxHealth = entity.getMaxHealth();
//        Random random = ScalingHealth.random;
//
//        final boolean makeBlight = shouldMakeBlight(entity, difficulty, config);
//        if (makeBlight) {
//            // TODO
////            difficulty *= config.mobs.
//        }

        // TODO: lunar cycles
//        if (Config.Difficulty.DIFFICULTY_LUNAR_MULTIPLIERS_ENABLED && world.getWorldTime() % 24000 > 12000) {
//            int moonPhase = world.provider.getMoonPhase(world.getWorldTime()) % 8;
//            float multi = Config.Difficulty.DIFFICULTY_LUNAR_MULTIPLIERS[moonPhase];
//            difficulty *= multi;
//        }
    }

    private static boolean healthIncreaseAllowed(EntityLivingBase entity, DimensionConfig config) {
        // TODO
        return true;
    }

    private static boolean shouldMakeBlight(EntityLivingBase entity, double difficulty, DimensionConfig config) {
        // TODO
        return false;
    }
}
