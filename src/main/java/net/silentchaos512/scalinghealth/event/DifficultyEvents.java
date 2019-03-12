package net.silentchaos512.scalinghealth.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.INBTBase;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultyAffected;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultySource;
import net.silentchaos512.scalinghealth.capability.CapabilityPlayerData;
import net.silentchaos512.scalinghealth.utils.Difficulty;
import net.silentchaos512.scalinghealth.utils.Players;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.lang.reflect.Field;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public final class DifficultyEvents {
    private static final boolean PRINT_DEBUG_INFO = true;

    public static final Marker MARKER = MarkerManager.getMarker("Difficulty");

    private DifficultyEvents() {}

    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (CapabilityDifficultyAffected.canAttachTo(entity)) {
//            debug(event::getObject);
            event.addCapability(CapabilityDifficultyAffected.NAME, new CapabilityDifficultyAffected());
        }
        if (CapabilityDifficultySource.canAttachTo(entity)) {
//            debug(() -> "Attaching difficulty source capability to " + entity);
            debug(() -> "attach difficulty source");
            event.addCapability(CapabilityDifficultySource.NAME, new CapabilityDifficultySource());
        }
        if (CapabilityPlayerData.canAttachTo(entity)) {
//            debug(() -> "Attaching player data capability to " + entity);
            debug(() -> "attach player data");
            event.addCapability(CapabilityPlayerData.NAME, new CapabilityPlayerData());
        }
    }

    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
        if (CapabilityDifficultySource.canAttachTo(world)) {
//            debug(() -> "Attaching difficulty source capability to " + world);
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
                float change = (float) Difficulty.changePerSecond(entity.world);
                source.setDifficulty(source.getDifficulty() + change);
//                debug(() -> String.format("Source %s difficulty is now %.5f", entity, source.getDifficulty()));
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
                float change = (float) Difficulty.changePerSecond(world);
                source.setDifficulty(source.getDifficulty() + change);
//                debug(() -> String.format("World %s difficulty is now %.5f", world, source.getDifficulty()));
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (player.world.isRemote) return;
        player.getCapability(CapabilityPlayerData.INSTANCE).ifPresent(data -> data.tick(player));
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // If not dead, player is returning from the End
        if (!event.isWasDeath()) return;

        // Player died. Copy capabilities and apply health/difficulty changes.
        EntityPlayer original = event.getOriginal();
        EntityPlayer clone = event.getEntityPlayer();
        debug(() -> "onPlayerClone");
        copyCapability(CapabilityPlayerData.INSTANCE, original, clone);
        copyCapability(CapabilityDifficultySource.INSTANCE, original, clone);

        // Apply death mutators
        clone.getCapability(CapabilityPlayerData.INSTANCE).ifPresent(data -> {
            int newCrystals = Players.getCrystalCountFromHealth(clone, Players.getHealthAfterDeath(clone, original.dimension));
            notifyOfChanges(clone, "heart crystal(s)", data.getExtraHearts(), newCrystals);
            data.setExtraHearts(clone, newCrystals);
        });
        clone.getCapability(CapabilityDifficultySource.INSTANCE).ifPresent(source -> {
            float newDifficulty = (float) Difficulty.getDifficultyAfterDeath(clone, original.dimension);
            notifyOfChanges(clone, "difficulty", source.getDifficulty(), newDifficulty);
            source.setDifficulty(newDifficulty);
        });
    }

    private static void notifyOfChanges(EntityPlayer player, String valueName, float oldValue, float newValue) {
        // TODO: Could also notify player in chat?
//        if (MathUtils.doublesEqual(oldValue, newValue)) return;
        float diff = newValue - oldValue;
        String line = String.format("%s %.2f %s", diff > 0 ? "gained" : "lost", diff, valueName);
        ScalingHealth.LOGGER.info("Player {}", line);
    }

    private static <T> void copyCapability(Capability<T> capability, ICapabilityProvider original, ICapabilityProvider clone) {
        // Temporary hack to work around Forge bug
        try {
            Field field = CapabilityProvider.class.getDeclaredField("capabilities");
            field.setAccessible(true);
            CapabilityDispatcher caps = (CapabilityDispatcher) field.get(original);
            caps.getCapability(capability).ifPresent(t -> {
                T tClone = clone.getCapability(capability).orElseThrow(IllegalStateException::new);
                INBTBase nbt = capability.getStorage().writeNBT(capability, t, null);
                capability.getStorage().readNBT(capability, tClone, null, nbt);
            });
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        // FIXME: Does not work because caps are invalidated
//        original.getCapability(capability).ifPresent(dataOriginal -> {
//            clone.getCapability(capability).ifPresent(dataClone -> {
//                INBTBase nbt = capability.getStorage().writeNBT(capability, dataOriginal, null);
//                capability.getStorage().readNBT(capability, dataClone, null, nbt);
//            });
//        });
    }

    private static void debug(Supplier<?> msg) {
        if (PRINT_DEBUG_INFO && ScalingHealth.LOGGER.isDebugEnabled()) {
            ScalingHealth.LOGGER.debug(MARKER, msg.get());
        }
    }
}
