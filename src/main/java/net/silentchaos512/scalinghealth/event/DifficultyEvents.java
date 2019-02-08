package net.silentchaos512.scalinghealth.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultyAffected;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultySource;
import net.silentchaos512.scalinghealth.capability.CapabilityPlayerData;
import net.silentchaos512.scalinghealth.config.DimensionConfig;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public final class DifficultyEvents {
    public static final Marker MARKER = MarkerManager.getMarker("Difficulty");

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
        if (CapabilityPlayerData.canAttachTo(entity)) {
            ScalingHealth.LOGGER.debug(MARKER, "Attaching player data capability to {}", entity);
            event.addCapability(CapabilityPlayerData.NAME, new CapabilityPlayerData());
        }
    }

    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
        if (CapabilityDifficultySource.canAttachTo(world)) {
            ScalingHealth.LOGGER.debug(MARKER, "Attaching difficulty source capability to {}", world);
            event.addCapability(CapabilityDifficultySource.NAME, new CapabilityDifficultySource());
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) return;

        // Tick mobs, which will calculate difficulty when appropriate and apply changes
        entity.getCapability(CapabilityDifficultyAffected.INSTANCE).ifPresent(affected -> affected.tick(entity));

        // Tick difficulty source, such as players
        if (entity.world.getGameTime() % 20 == 0) {
            entity.getCapability(CapabilityDifficultySource.INSTANCE).ifPresent(source -> {
                source.setDifficulty(source.getDifficulty() + 0.001f);
                ScalingHealth.LOGGER.debug(MARKER, "Source {} difficulty is now {}", entity, source.getDifficulty());
            });
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (world.isRemote) return;

        // Tick world difficulty source
        if (world.getGameTime() % 20 == 0) {
            world.getCapability(CapabilityDifficultySource.INSTANCE).ifPresent(source -> {
                source.setDifficulty(source.getDifficulty() + 0.001f);
                ScalingHealth.LOGGER.debug(MARKER, "World {} difficulty is now {}", world, source.getDifficulty());
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (player.world.isRemote) return;
        player.getCapability(CapabilityPlayerData.INSTANCE).ifPresent(data -> data.tick(player));
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
