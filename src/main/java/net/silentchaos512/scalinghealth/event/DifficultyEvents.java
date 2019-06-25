package net.silentchaos512.scalinghealth.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.DifficultyAffectedCapability;
import net.silentchaos512.scalinghealth.capability.DifficultySourceCapability;
import net.silentchaos512.scalinghealth.capability.PlayerDataCapability;
import net.silentchaos512.scalinghealth.utils.Difficulty;
import net.silentchaos512.scalinghealth.utils.Players;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public final class DifficultyEvents {
    private static final boolean PRINT_DEBUG_INFO = true;

    public static final Marker MARKER = MarkerManager.getMarker("Difficulty");

    private DifficultyEvents() {}

    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (DifficultyAffectedCapability.canAttachTo(entity)) {
//            debug(event::getObject);
            event.addCapability(DifficultyAffectedCapability.NAME, new DifficultyAffectedCapability());
        }
        if (DifficultySourceCapability.canAttachTo(entity)) {
//            debug(() -> "Attaching difficulty source capability to " + entity);
            debug(() -> "attach difficulty source");
            event.addCapability(DifficultySourceCapability.NAME, new DifficultySourceCapability());
        }
        if (PlayerDataCapability.canAttachTo(entity)) {
//            debug(() -> "Attaching player data capability to " + entity);
            debug(() -> "attach player data");
            event.addCapability(PlayerDataCapability.NAME, new PlayerDataCapability());
        }
    }

    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
        if (DifficultySourceCapability.canAttachTo(world)) {
//            debug(() -> "Attaching difficulty source capability to " + world);
            event.addCapability(DifficultySourceCapability.NAME, new DifficultySourceCapability());
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.world.isRemote) return;

        // Tick mobs, which will calculate difficulty when appropriate and apply changes
        if (entity instanceof MobEntity) {
            entity.getCapability(DifficultyAffectedCapability.INSTANCE).ifPresent(affected -> {
                affected.tick((MobEntity) entity);
            });
        }

        // Tick difficulty source, such as players
        if (entity.world.getGameTime() % 20 == 0) {
            entity.getCapability(DifficultySourceCapability.INSTANCE).ifPresent(source -> {
                float change = (float) Difficulty.changePerSecond(entity.world);
                source.setDifficulty(source.getDifficulty() + change);
            });
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (world.isRemote) return;

        // Tick world difficulty source
        if (world.getGameTime() % 20 == 0) {
            world.getCapability(DifficultySourceCapability.INSTANCE).ifPresent(source -> {
                float change = (float) Difficulty.changePerSecond(world);
                source.setDifficulty(source.getDifficulty() + change);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        if (player.world.isRemote) return;
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(data -> data.tick(player));
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // If not dead, player is returning from the End
        if (!event.isWasDeath()) return;

        // Player died. Copy capabilities and apply health/difficulty changes.
        PlayerEntity original = event.getOriginal();
        PlayerEntity clone = event.getEntityPlayer();
        debug(() -> "onPlayerClone");
        copyCapability(PlayerDataCapability.INSTANCE, original, clone);
        copyCapability(DifficultySourceCapability.INSTANCE, original, clone);

        // Apply death mutators
        clone.getCapability(PlayerDataCapability.INSTANCE).ifPresent(data -> {
            int newCrystals = Players.getCrystalCountFromHealth(clone, Players.getHealthAfterDeath(clone, original.dimension));
            notifyOfChanges(clone, "heart crystal(s)", data.getExtraHearts(), newCrystals);
            data.setExtraHearts(clone, newCrystals);
        });
        clone.getCapability(DifficultySourceCapability.INSTANCE).ifPresent(source -> {
            float newDifficulty = (float) Difficulty.getDifficultyAfterDeath(clone, original.dimension);
            notifyOfChanges(clone, "difficulty", source.getDifficulty(), newDifficulty);
            source.setDifficulty(newDifficulty);
        });
    }

    private static void notifyOfChanges(PlayerEntity player, String valueName, float oldValue, float newValue) {
        // TODO: Could also notify player in chat?
//        if (MathUtils.doublesEqual(oldValue, newValue)) return;
        float diff = newValue - oldValue;
        String line = String.format("%s %.2f %s", diff > 0 ? "gained" : "lost", diff, valueName);
        ScalingHealth.LOGGER.info("Player {}", line);
    }

    private static <T> void copyCapability(Capability<T> capability, ICapabilityProvider original, ICapabilityProvider clone) {
        // Temporary hack to work around Forge bug
/*        try {
            Field field = CapabilityProvider.class.getDeclaredField("capabilities");
            field.setAccessible(true);
            CapabilityDispatcher caps = (CapabilityDispatcher) field.get(original);
            caps.getCapability(capability).ifPresent(t -> {
                T tClone = clone.getCapability(capability).orElseThrow(IllegalStateException::new);
                INBT nbt = capability.getStorage().writeNBT(capability, t, null);
                capability.getStorage().readNBT(capability, tClone, null, nbt);
            });
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }*/

        original.getCapability(capability).ifPresent(dataOriginal -> {
            clone.getCapability(capability).ifPresent(dataClone -> {
                INBT nbt = capability.getStorage().writeNBT(capability, dataOriginal, null);
                capability.getStorage().readNBT(capability, dataClone, null, nbt);
            });
        });
    }

    private static void debug(Supplier<?> msg) {
        if (PRINT_DEBUG_INFO && ScalingHealth.LOGGER.isDebugEnabled()) {
            ScalingHealth.LOGGER.debug(MARKER, msg.get());
        }
    }
}
