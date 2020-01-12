package net.silentchaos512.scalinghealth.event;

import net.minecraft.client.gui.NewChatGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
            event.addCapability(DifficultyAffectedCapability.NAME, new DifficultyAffectedCapability());
        }
        if (DifficultySourceCapability.canAttachTo(entity)) {
            debug(() -> "attach difficulty source");
            event.addCapability(DifficultySourceCapability.NAME, new DifficultySourceCapability());
        }
        if (PlayerDataCapability.canAttachTo(entity)) {
            debug(() -> "attach player data");
            event.addCapability(PlayerDataCapability.NAME, new PlayerDataCapability());
        }
    }

    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
        if (DifficultySourceCapability.canAttachTo(world)) {
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

        // Player died. Copy capabilities before applying health/difficulty changes.
        PlayerEntity original = event.getOriginal();
        PlayerEntity clone = event.getPlayer();
        debug(() -> "onPlayerClone");
        copyCapability(PlayerDataCapability.INSTANCE, original, clone);
        copyCapability(DifficultySourceCapability.INSTANCE, original, clone);

        // Apply death mutators
        clone.getCapability(PlayerDataCapability.INSTANCE).ifPresent(data -> {
            int newCrystals = Players.getCrystalCountFromHealth(original, Players.getHealthAfterDeath(original, original.dimension));
            notifyOfChanges(clone, "heart crystal(s)", data.getExtraHearts(), newCrystals);
            data.setExtraHearts(clone, newCrystals);
        });
        clone.getCapability(DifficultySourceCapability.INSTANCE).ifPresent(source -> {
            float newDifficulty = (float) Difficulty.getDifficultyAfterDeath(clone, original.dimension);    //not sure to pass the clone or the og (the dim is good)
            notifyOfChanges(clone, "difficulty", source.getDifficulty(), newDifficulty);
            source.setDifficulty(newDifficulty);
        });
    }

    @SubscribeEvent
    public static void onPlayerJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(data -> {
            ScalingHealth.LOGGER.info("Updating stats for {}", player.getScoreboardName());
            data.updateStats(player);
        });
    }

    private static void notifyOfChanges(PlayerEntity player, String valueName, float oldValue, float newValue) {
        float diff = newValue - oldValue;
        String line = String.format("%s %.2f %s", diff > 0 ? "gained" : "lost", diff, valueName);
        if(diff != 0)
            player.sendMessage(new StringTextComponent(line));
        ScalingHealth.LOGGER.info("Player {}", line);
    }

    private static <T> void copyCapability(Capability<T> capability, ICapabilityProvider original, ICapabilityProvider clone) {
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
