package net.silentchaos512.scalinghealth.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
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
        if (entity.world.isRemote || (entity.world.getPlayers().isEmpty() && !((ServerWorld)entity.world).getServer().isDedicatedServer()))
            return;

        // Tick mobs, which will calculate difficulty when appropriate and apply changes
        if (entity instanceof MobEntity)
            entity.getCapability(DifficultyAffectedCapability.INSTANCE).ifPresent(data ->
                    data.tick((MobEntity)entity));

        if(entity instanceof TameableEntity) {
            if(!((TameableEntity) entity).isTamed()) return;
                entity.getCapability(PetHealthCapability.INSTANCE).ifPresent(data ->
                        data.tick((TameableEntity) entity));
        }

        if (entity instanceof PlayerEntity && entity.world.getGameTime() % 20 == 0) {
            entity.getCapability(DifficultySourceCapability.INSTANCE).ifPresent(source -> {
                source.addDifficulty((float) SHDifficulty.changePerSecond());
            });
        }
    }

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        LivingEntity killed = event.getEntityLiving();
        if (event.getSource() == null || event.getEntity().world.isRemote)
            return;

        Entity entitySource = event.getSource().getTrueSource();
        if (entitySource instanceof PlayerEntity) {
            SHDifficulty.applyKillMutator(killed, (PlayerEntity) entitySource);
            return;
        }

        if(entitySource instanceof TameableEntity && ((TameableEntity) entitySource).isTamed()) {
            TameableEntity pet = (TameableEntity) entitySource;
            if(pet.getOwner() instanceof PlayerEntity)
                SHDifficulty.applyKillMutator(killed, (PlayerEntity) pet.getOwner());
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if(event.phase == TickEvent.Phase.START) return;
        World world = event.world;
        if (world.isRemote) return;

        // Tick world difficulty source
        if (world.getGameTime() % 20 == 0) {
            world.getCapability(DifficultySourceCapability.INSTANCE).ifPresent(source -> {
                float change = (float) SHDifficulty.changePerSecond();
                source.setDifficulty(source.getDifficulty() + change);
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Player is cloned. Copy capabilities before applying health/difficulty changes if needed.
        PlayerEntity original = event.getOriginal();
        PlayerEntity clone = event.getPlayer();

        debug(() -> "onPlayerClone");
        copyCapability(PlayerDataCapability.INSTANCE, original, clone);
        copyCapability(DifficultySourceCapability.INSTANCE, original, clone);

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

    private static void notifyOfChanges(PlayerEntity player, String valueName, float oldValue, float newValue) {
        float diff = newValue - oldValue;
        String line = String.format("%s %.2f %s", diff > 0 ? "gained" : "lost", diff, valueName);
        if(diff != 0)
            player.sendMessage(new StringTextComponent(line), Util.DUMMY_UUID);
        ScalingHealth.LOGGER.info("Player {}", line);
    }

    private static <T> void copyCapability(Capability<T> capability, ICapabilityProvider original, ICapabilityProvider clone) {
        original.getCapability(capability).ifPresent(dataOriginal ->
            clone.getCapability(capability).ifPresent(dataClone -> {
                INBT nbt = capability.getStorage().writeNBT(capability, dataOriginal, null);
                capability.getStorage().readNBT(capability, dataClone, null, nbt);
            }));
    }

    private static void debug(Supplier<?> msg) {
        if (SHConfig.SERVER.debugMaster.get())
            ScalingHealth.LOGGER.debug(MARKER, msg.get());
    }
}
