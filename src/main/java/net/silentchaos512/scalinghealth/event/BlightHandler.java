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

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.DimensionConfig;
import net.silentchaos512.scalinghealth.entity.BlightFireEntity;
import net.silentchaos512.scalinghealth.network.Network;
import net.silentchaos512.scalinghealth.network.SpawnBlightFirePacket;
import net.silentchaos512.scalinghealth.utils.Difficulty;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public final class BlightHandler {
    private static final int UPDATE_DELAY = 200;

    private BlightHandler() {}

    // ******************
    // * Blight marking *
    // ******************

    @SuppressWarnings("TypeMayBeWeakened")
    public static boolean isBlight(MobEntity entityLiving) {
        return Difficulty.affected(entityLiving).isBlight();
    }

    public static void markBlight(MobEntity entityLiving) {
//        if (entityLiving != null)
//            entityLiving.getEntityData().setBoolean(NBT_BLIGHT, true);
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
            }
        }

        return null;
    }

    private static void applyBlightPotionEffects(MobEntity entityLiving) {
        DimensionConfig config = Config.get(entityLiving);
        config.mobs.blightPotions.applyAll(entityLiving);
    }

    // **********
    // * Events *
    // **********

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlightKilled(LivingDeathEvent event) {
        /*
        if (event.getSource() == null || !isBlight(event.getEntityLiving()) || event.getEntity().world.isRemote)
            return;

        Entity entitySource = event.getSource().getTrueSource();
        boolean isTamedAnimal = entitySource instanceof EntityTameable && ((EntityTameable) entitySource).isTamed();
        if (entitySource instanceof EntityPlayer || isTamedAnimal) {
            // Killed by a player or a player's pet.
            MobEntity blight = event.getEntityLiving();
            EntityPlayer player;
            MobEntity actualKiller;
            if (isTamedAnimal) {
                player = (EntityPlayer) ((EntityTameable) entitySource).getOwner();
                actualKiller = (MobEntity) entitySource;
            } else {
                actualKiller = player = (EntityPlayer) entitySource;
            }

            // Tell all players that the blight was killed.
            if (Config.Mob.Blight.notifyOnDeath && player != null) {
                ScalingHealth.logHelper.info("Blight {} was killed by {}", blight.getName(), actualKiller.getName());
                for (EntityPlayer p : player.world.getPlayers(EntityPlayer.class, e -> true)) {
                    // FIXME: blight name translation
                    ChatHelper.translate(p, ScalingHealth.i18n.getKey("blight", "killedByPlayer"), "Blight " + blight.getName(), actualKiller.getName());
                }
            }

            // Drop hearts!
            final boolean canGetHearts = !(player instanceof FakePlayer) || Config.FakePlayer.generateHearts;
            final int min = Config.Items.Heart.blightMin;
            final int max = Config.Items.Heart.blightMax;
            final int heartCount = ScalingHealth.random.nextInt(max - min + 1) + min;

            if (canGetHearts && heartCount > 0) {
                Item itemToDrop = Config.Items.Heart.dropShardsInstead ? ModItems.crystalShard : ModItems.heart;
                blight.dropItem(itemToDrop, heartCount);
            }
        } else {
            // Killed by something else.
            MobEntity blight = event.getEntityLiving();

            // Tell all players that the blight died.
            if (Config.Mob.Blight.notifyOnDeath) {
                ITextComponent deathMessage = event.getSource().getDeathMessage(blight);
                if (deathMessage instanceof TextComponentTranslation) {
                    // Assuming arguments are the same as in DamageSource#getDeathMessage
                    // May fail with some modded damage sources, but should be fine in most cases
                    TextComponentTranslation original = (TextComponentTranslation) deathMessage;
                    // FIXME: blight name translation
                    TextComponentTranslation newMessage = new TextComponentTranslation(original.getKey(),
                            "Blight " + blight.getName());
                    ScalingHealth.logHelper.info("Blight {} has died", blight.getName());
                    for (EntityPlayer p : blight.world.getPlayers(EntityPlayer.class, e -> true))
                        ChatHelper.sendMessage(p, newMessage);
                }

                // FIXME
//                if (message.contains("drowned")) {
//                    if (message.startsWith("Blight Squid"))
//                        message += "... again";
//                    else
//                        message += "... gg";
//                } else if (message.contains("suffocated in a wall")) {
//                    message += " *slow clap*";
//                }
            }
        }
        */
    }

    @SubscribeEvent
    public static void onBlightUpdate(LivingUpdateEvent event) {
        LivingEntity livingEntity = event.getEntityLiving();
        if (!(livingEntity instanceof MobEntity)) return;

        MobEntity entity = (MobEntity) livingEntity;
        if (!entity.world.isRemote && isBlight(entity)) {
            World world = entity.world;

            // Add in entity ID so not all blights update on the same tick
            if ((world.getGameTime() + entity.getEntityId()) % UPDATE_DELAY == 0) {
                // Send message to clients to make sure they know the entity is a blight.
//                MessageMarkBlight message = new MessageMarkBlight(entity);
//                NetworkHandler.INSTANCE.sendToAllAround(message, new PacketDistributor.TargetPoint(entity.dimension,
//                        entity.posX, entity.posY, entity.posZ, 128));

                // Effects
                // Assign a blight fire if necessary.
                if (getBlightFire(entity) == null) {
                    spawnBlightFire(entity);
                }

                // Refresh potion effects
                applyBlightPotionEffects(entity);
            }
        }
    }
}
