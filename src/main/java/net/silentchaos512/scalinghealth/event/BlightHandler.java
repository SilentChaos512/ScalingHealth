/*
 * Scaling Health
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.scalinghealth.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.IDifficultyAffected;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.DimensionConfig;
import net.silentchaos512.scalinghealth.entity.BlightFireEntity;
import net.silentchaos512.scalinghealth.network.MarkBlightPacket;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.network.SpawnBlightFirePacket;
import net.silentchaos512.scalinghealth.utils.Difficulty;
import net.silentchaos512.scalinghealth.utils.MobDifficultyHandler;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public final class BlightHandler {

    public static final String NBT_BLIGHT = ScalingHealth.MOD_ID + ".IsBlight";
    private static final int UPDATE_DELAY = 200;

    private BlightHandler() {}

    // ******************
    // * Blight marking *
    // ******************

    @SuppressWarnings("TypeMayBeWeakened")
    public static boolean isBlight(MobEntity entity) {
        return Difficulty.affected(entity).isBlight();
    }

    public static void markBlight(MobEntity entity) {
        if (entity != null){
            IDifficultyAffected data = Difficulty.affected(entity);
            MobDifficultyHandler.setEntityProperties(entity, data , data.getDifficulty(), true);
        }
    }

    private static void spawnBlightFire(MobEntity blight) {
        if (blight.world.isRemote || getBlightFire(blight) != null) return;

        BlightFireEntity fire = new BlightFireEntity(blight);
        fire.setPosition(blight.posX, blight.posY, blight.posZ);
        blight.world.addEntity(fire);

        SpawnBlightFirePacket packet = new SpawnBlightFirePacket(blight);
        Supplier<PacketDistributor.TargetPoint> target = PacketDistributor.TargetPoint.p(blight.posX, blight.posY, blight.posZ, 128, blight.dimension);
        Network.channel.send(PacketDistributor.NEAR.with(target), packet);

        if (ScalingHealth.LOGGER.isDebugEnabled()) {
            ScalingHealth.LOGGER.debug("Spawned blight fire for {}", blight);
        }
    }

    @Nullable
    private static BlightFireEntity getBlightFire(MobEntity blight) {
        for (BlightFireEntity fire : blight.world.getEntitiesWithinAABB(BlightFireEntity.class, blight.getBoundingBox().grow(5))) {
            if (blight.equals(fire.getParent())) {
                return fire;
            }   else {
                ScalingHealth.LOGGER.debug("Is there another Blight near this one? Only reason this should be called");
            }
        }
        return null;
    }

    private static void applyBlightPotionEffects(MobEntity entityLiving) {
        DimensionConfig config = Config.get(entityLiving);
        config.mobs.blightPotions.applyAll(entityLiving);
    }

    private static void notifyPlayers(ITextComponent deathMessage, MobEntity blight){
        if (deathMessage instanceof TranslationTextComponent) {
            // Assuming arguments are the same as in DamageSource#getDeathMessage
            // May fail with some modded damage sources, but should be fine in most cases
            TranslationTextComponent original = (TranslationTextComponent) deathMessage;

            StringTextComponent s = new StringTextComponent("Blight " + blight.getName().getString());
            s.setStyle(new Style().setColor(TextFormatting.DARK_PURPLE));

            TranslationTextComponent newMessage = new TranslationTextComponent(original.getKey(), s);
            StringTextComponent finalMessage = new StringTextComponent(newMessage.getFormattedText());
            String message = newMessage.getString();

            if(message.contains("drowned")){
                if(message.startsWith("Blight Squid")){
                    finalMessage = new StringTextComponent(finalMessage.getFormattedText() + "... again");
                }
                else
                    finalMessage = new StringTextComponent(finalMessage.getFormattedText() + "... gg");
            } else if(message.contains("suffocated in a wall")){
                finalMessage = new StringTextComponent(finalMessage.getFormattedText() + " *slow clap*");
            }

            for (PlayerEntity p : blight.world.getPlayers())
                p.sendMessage(finalMessage);
        }
    }

    // **********
    // * Events *
    // **********

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlightKilled(LivingDeathEvent event) {
        if(!(event.getEntityLiving() instanceof MobEntity)) return;

        MobEntity blight = (MobEntity) event.getEntityLiving();
        if (event.getSource() == null || !isBlight(blight) || event.getEntity().world.isRemote)
            return;

        Entity entitySource = event.getSource().getTrueSource();
        boolean isTamedAnimal = entitySource instanceof TameableEntity && ((TameableEntity) entitySource).isTamed();
        if (entitySource instanceof PlayerEntity || isTamedAnimal) {
            // Killed by a player or a player's pet.
            PlayerEntity player;
            LivingEntity actualKiller;
            if (isTamedAnimal) {
                player = (PlayerEntity) ((TameableEntity) entitySource).getOwner();
                actualKiller = (MobEntity) entitySource;
            } else {
                actualKiller = player = (PlayerEntity) entitySource;
            }

            // Tell all players that the blight was killed.
            if (Difficulty.notifyOnDeath(blight.world) && player != null) {
                ScalingHealth.LOGGER.info("Blight {} was killed by {}", blight.getName().getString(), actualKiller.getName().getString());
                notifyPlayers(event.getSource().getDeathMessage(blight), blight);
            }
        } else {
            // Killed by something else.
            // Tell all players that the blight died.
            if (Difficulty.notifyOnDeath(blight.world))
                ScalingHealth.LOGGER.info("Blight {} has died", blight.getName().getString());
                notifyPlayers(event.getSource().getDeathMessage(blight), blight);
        }
    }

    @SubscribeEvent
    public static void onBlightUpdate(LivingUpdateEvent event) {
        LivingEntity livingEntity = event.getEntityLiving();
        if (!(livingEntity instanceof MobEntity)) return;

        MobEntity blight = (MobEntity) livingEntity;
        if (!blight.world.isRemote && isBlight(blight)) {
            World world = blight.world;

            if(!blight.getPassengers().isEmpty())
                ScalingHealth.LOGGER.debug("{} #{} has passenger {}", blight.getName().getString(), blight.getEntityId(), blight.getPassengers().get(0).getName().getString());

            // Add in entity ID so not all blights update on the same tick
            if ((world.getGameTime() + blight.getEntityId()) % UPDATE_DELAY == 0) {
                spawnBlightFire(blight);
                // Refresh potion effects
                applyBlightPotionEffects(blight);
            }
        }
    }
}
