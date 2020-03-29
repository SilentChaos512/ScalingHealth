package net.silentchaos512.scalinghealth.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.*;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.utils.EnabledFeatures;
import net.silentchaos512.scalinghealth.utils.SHDifficulty;
import net.silentchaos512.scalinghealth.utils.SHPlayers;
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
        if (Config.COMMON.enableDifficulty.get() && DifficultySourceCapability.canAttachTo(world)) {
            debug(()->"attach source to world");
            DifficultySourceCapability cap = new DifficultySourceCapability();
            event.addCapability(DifficultySourceCapability.NAME, cap);
            DifficultySourceCapability.setOverworldCap(cap);
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.world.isRemote) return;

        // Tick mobs, which will calculate difficulty when appropriate and apply changes
        if (entity instanceof MobEntity)
            SHDifficulty.affected(entity).tick((MobEntity) entity);

        if(entity instanceof TameableEntity) {
            if(!((TameableEntity) entity).isTamed()) return;
                entity.getCapability(PetHealthCapability.INSTANCE).ifPresent(data ->
                        data.tick((TameableEntity) entity));
        }

        // Tick difficulty source, such as players, except if exempted
        if (entity instanceof PlayerEntity && entity.world.getGameTime() % 20 == 0) {
            IDifficultySource source = SHDifficulty.source(entity);

            boolean exempt = SHDifficulty.isPlayerExempt((PlayerEntity) event.getEntityLiving());
            source.setExempt(exempt);
            if(exempt) return;
            source.addDifficulty((float) SHDifficulty.changePerSecond());
        }
    }

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        if(!(event.getEntityLiving() instanceof MobEntity)) return;
        MobEntity entity = (MobEntity) event.getEntityLiving();
        if (event.getSource() == null || event.getEntity().world.isRemote)
            return;

        Entity entitySource = event.getSource().getTrueSource();
        boolean isTamedAnimal = entitySource instanceof TameableEntity && ((TameableEntity) entitySource).isTamed();
        if (entitySource instanceof PlayerEntity ) {
            SHDifficulty.setSourceDifficulty((PlayerEntity) entitySource, SHDifficulty.applyKillMutator(entity, (PlayerEntity) entitySource));
            return;
        }
        if(isTamedAnimal){
            TameableEntity pet = (TameableEntity) entitySource;
            if(pet.getOwner() instanceof PlayerEntity)
                SHDifficulty.setSourceDifficulty(((PlayerEntity) pet.getOwner()), SHDifficulty.applyKillMutator(entity, (PlayerEntity) pet.getOwner()));
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

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(event.phase == TickEvent.Phase.START) return;
        PlayerEntity player = event.player;
        if (player.world.isRemote) return;
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(data -> data.tick(player));
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Player is cloned. Copy capabilities before applying health/difficulty changes if needed.
        PlayerEntity original = event.getOriginal();
        PlayerEntity clone = event.getPlayer();
        SHPlayers.getPlayerData(original).setXpHearts(original, clone.experienceLevel);

        debug(() -> "onPlayerClone");
        copyCapability(PlayerDataCapability.INSTANCE, original, clone);
        copyCapability(DifficultySourceCapability.INSTANCE, original, clone);

        // If not dead, player is returning from the End
        if (!event.isWasDeath()) return;

        // Apply death mutators
        clone.getCapability(PlayerDataCapability.INSTANCE).ifPresent(data -> {
            int newCrystals = SHPlayers.getCrystalCountFromHealth(SHPlayers.getHealthAfterDeath(original));
            notifyOfChanges(clone, "heart crystal(s)", data.getHeartByCrystals(), newCrystals);
            data.setHeartByCrystals(clone, newCrystals);
        });
        clone.getCapability(DifficultySourceCapability.INSTANCE).ifPresent(source -> {
            float newDifficulty = (float) SHDifficulty.getDifficultyAfterDeath(clone);
            notifyOfChanges(clone, "difficulty", source.getDifficulty(), newDifficulty);
            source.setDifficulty(newDifficulty);
        });
    }

    @SubscribeEvent
    public static void onPlayerJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();
        ScalingHealth.LOGGER.info("Updating stats for {}", player.getCustomName());
        SHPlayers.getPlayerData(player).updateStats(player);
    }

    private static void notifyOfChanges(PlayerEntity player, String valueName, float oldValue, float newValue) {
        float diff = newValue - oldValue;
        String line = String.format("%s %.2f %s", diff > 0 ? "gained" : "lost", diff, valueName);
        if(diff != 0)
            player.sendMessage(new StringTextComponent(line));
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
        if (PRINT_DEBUG_INFO && ScalingHealth.LOGGER.isDebugEnabled())
            ScalingHealth.LOGGER.debug(MARKER, msg.get());
    }
}
