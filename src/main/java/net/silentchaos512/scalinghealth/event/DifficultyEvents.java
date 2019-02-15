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
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public final class DifficultyEvents {
    private static final boolean PRINT_DEBUG_INFO = false;

    public static final Marker MARKER = MarkerManager.getMarker("Difficulty");

    private DifficultyEvents() {}

    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (CapabilityDifficultyAffected.canAttachTo(entity)) {
            debug(event::getObject);
            event.addCapability(CapabilityDifficultyAffected.NAME, new CapabilityDifficultyAffected());
        }
        if (CapabilityDifficultySource.canAttachTo(entity)) {
            debug(() -> "Attaching difficulty source capability to " + entity);
            event.addCapability(CapabilityDifficultySource.NAME, new CapabilityDifficultySource());
        }
        if (CapabilityPlayerData.canAttachTo(entity)) {
            debug(() -> "Attaching player data capability to " + entity);
            event.addCapability(CapabilityPlayerData.NAME, new CapabilityPlayerData());
        }
    }

    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
        if (CapabilityDifficultySource.canAttachTo(world)) {
            debug(() -> "Attaching difficulty source capability to " + world);
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
                debug(() -> String.format("Source %s difficulty is now %.5f", entity, source.getDifficulty()));
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
                debug(() -> String.format("World %s difficulty is now %.5f", world, source.getDifficulty()));
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (player.world.isRemote) return;
        player.getCapability(CapabilityPlayerData.INSTANCE).ifPresent(data -> data.tick(player));
    }

    private static void debug(Supplier<?> msg) {
        if (PRINT_DEBUG_INFO && ScalingHealth.LOGGER.isDebugEnabled()) {
            ScalingHealth.LOGGER.debug(MARKER, msg.get());
        }
    }
}
