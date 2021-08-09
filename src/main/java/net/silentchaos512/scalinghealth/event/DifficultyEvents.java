package net.silentchaos512.scalinghealth.event;

import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.DifficultyAffectedCapability;
import net.silentchaos512.scalinghealth.capability.DifficultySourceCapability;
import net.silentchaos512.scalinghealth.capability.PetHealthCapability;
import net.silentchaos512.scalinghealth.capability.PlayerDataCapability;
import net.silentchaos512.scalinghealth.config.SHConfig;
import net.silentchaos512.scalinghealth.utils.config.EnabledFeatures;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;
import net.silentchaos512.scalinghealth.utils.config.SHPlayers;
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
        if (DifficultyAffectedCapability.canAttachTo(entity)) {
            event.addCapability(DifficultyAffectedCapability.NAME, new DifficultyAffectedCapability());
        }
        if (EnabledFeatures.difficultyEnabled() && DifficultySourceCapability.canAttachTo(entity)) {
            debug(() -> "attach source to player");
            event.addCapability(DifficultySourceCapability.NAME, new DifficultySourceCapability());
        }
        if (PlayerDataCapability.canAttachTo(entity)) {
            debug(() -> "attach player data");
            event.addCapability(PlayerDataCapability.NAME, new PlayerDataCapability());
        }
        if(EnabledFeatures.petBonusHpEnabled() && PetHealthCapability.canAttachTo(entity)){
            debug(()-> "attach pet data");
            event.addCapability(PetHealthCapability.NAME, new PetHealthCapability());
        }
    }

    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<Level> event) {
        Level world = event.getObject();
        if (SHConfig.SERVER.enableDifficulty.get() && DifficultySourceCapability.canAttachTo(world)) {
            debug(()->"attach source to world");
            DifficultySourceCapability cap = new DifficultySourceCapability();
            event.addCapability(DifficultySourceCapability.NAME, cap);
            DifficultySourceCapability.setOverworldCap(cap);
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        //Return if players are empty on an integrated server, as the player needs a small delay to connect.
        if (entity.level.isClientSide || (entity.level.players().isEmpty() && !((ServerLevel)entity.level).getServer().isDedicatedServer()))
            return;

        // Tick mobs, which will calculate difficulty when appropriate and apply changes
        if (entity instanceof Mob)
            entity.getCapability(DifficultyAffectedCapability.INSTANCE).ifPresent(data ->
                    data.tick((Mob)entity));

        if(entity instanceof TamableAnimal) {
            if(!((TamableAnimal) entity).isTame()) return;
                entity.getCapability(PetHealthCapability.INSTANCE).ifPresent(data ->
                        data.tick((TamableAnimal) entity));
        }

        if (entity instanceof Player && entity.level.getGameTime() % 20 == 0) {
            entity.getCapability(DifficultySourceCapability.INSTANCE).ifPresent(source -> {
                source.addDifficulty((float) SHDifficulty.changePerSecond());
            });
        }
    }

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        LivingEntity killed = event.getEntityLiving();
        if (event.getSource() == null || event.getEntity().level.isClientSide)
            return;

        Entity entitySource = event.getSource().getEntity();
        if (entitySource instanceof Player) {
            SHDifficulty.applyKillMutator(killed, (Player) entitySource);
            return;
        }

        if(entitySource instanceof TamableAnimal && ((TamableAnimal) entitySource).isTame()) {
            TamableAnimal pet = (TamableAnimal) entitySource;
            if(pet.getOwner() instanceof Player)
                SHDifficulty.applyKillMutator(killed, (Player) pet.getOwner());
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if(event.phase == TickEvent.Phase.START) return;
        Level world = event.world;
        if (world.isClientSide) return;

        // Tick world difficulty source
        if (world.getGameTime() % 20 == 0) {
            world.getCapability(DifficultySourceCapability.INSTANCE).ifPresent(source -> {
                float change = (float) SHDifficulty.changePerSecond();
                source.setDifficulty(source.getDifficulty() + change);
            });
        }
    }

    private static Field validCap;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (validCap == null) {
            try {
                validCap = CapabilityProvider.class.getDeclaredField("valid");
                validCap.setAccessible(true);
            } catch (Exception e) {
                throw new RuntimeException("Could not access field!", e);
            }
        }

        // Player is cloned. Copy capabilities before applying health/difficulty changes if needed.
        Player original = event.getOriginal();
        Player clone = event.getPlayer();

        //TODO replace with reviveCaps() once forge calls super in LivingEntity
        try {
            validCap.set(original, true);
        } catch (Exception e) {
            throw new RuntimeException("Could not set capability field!");
        }

        copyCapability(PlayerDataCapability.INSTANCE, original, clone);
        copyCapability(DifficultySourceCapability.INSTANCE, original, clone);
        original.invalidateCaps();

        // If not dead, player is returning from the End
        if (!event.isWasDeath()) return;

        // Apply death mutators
        clone.getCapability(PlayerDataCapability.INSTANCE).ifPresent(data -> {
            data.updateStats(clone);
            int newCrystals = SHPlayers.getCrystalsAfterDeath(clone);
            notifyOfChanges(clone, "heart crystal(s)", data.getHeartCrystals(), newCrystals);
            data.setHeartCrystals(clone, newCrystals);
        });

        clone.getCapability(DifficultySourceCapability.INSTANCE).ifPresent(source -> {
            float newDifficulty = (float) SHDifficulty.getDifficultyAfterDeath(clone);
            notifyOfChanges(clone, "difficulty", source.getDifficulty(), newDifficulty);
            source.setDifficulty(newDifficulty);
        });
    }

    private static void notifyOfChanges(Player player, String valueName, float oldValue, float newValue) {
        float diff = newValue - oldValue;
        String line = String.format("%s %.2f %s", diff > 0 ? "gained" : "lost", diff, valueName);
        if(diff != 0)
            player.sendMessage(new TextComponent(line), Util.NIL_UUID);
        ScalingHealth.LOGGER.info("Player {}", line);
    }

    private static <T> void copyCapability(Capability<T> capability, ICapabilityProvider original, ICapabilityProvider clone) {
        original.getCapability(capability).ifPresent(dataOriginal ->
            clone.getCapability(capability).ifPresent(dataClone -> {
                if(dataOriginal instanceof INBTSerializable originalS && dataClone instanceof INBTSerializable cloneS) {
                    cloneS.deserializeNBT(originalS.serializeNBT());
                }
            }));
    }

    private static void debug(Supplier<?> msg) {
        if (SHConfig.SERVER.debugMaster.get())
            ScalingHealth.LOGGER.debug(MARKER, msg.get());
    }
}
