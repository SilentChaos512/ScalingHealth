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

package net.silentchaos512.scalinghealth.proxy;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.silentchaos512.lib.proxy.IProxy;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.lib.util.Color;
import net.silentchaos512.scalinghealth.compat.morpheus.SHMorpheusCompat;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.event.*;
import net.silentchaos512.scalinghealth.init.*;
import net.silentchaos512.scalinghealth.lib.EnumModParticles;
import net.silentchaos512.scalinghealth.network.NetworkHandler;
import net.silentchaos512.scalinghealth.network.message.MessagePlaySound;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.world.SHWorldGenerator;

public class ScalingHealthCommonProxy implements IProxy {

    @Override
    public void preInit(SRegistry registry, FMLPreInitializationEvent event) {
        Config.INSTANCE.init(event.getSuggestedConfigurationFile());

        registry.addRegistrationHandler(new ModPotions(), Potion.class);
        registry.addRegistrationHandler(new ModBlocks(), Block.class);
        registry.addRegistrationHandler(new ModItems(), Item.class);
        registry.addRegistrationHandler(new ModSounds(), SoundEvent.class);

        GameRegistry.registerWorldGenerator(new SHWorldGenerator(true), 0);

        ModEntities.init(registry);
        DifficultyHandler.INSTANCE.initDefaultEquipment();

        NetworkHandler.init();

        MinecraftForge.EVENT_BUS.register(new ScalingHealthCommonEvents());
        MinecraftForge.EVENT_BUS.register(new SHPlayerDataHandler.EventHandler());
        MinecraftForge.EVENT_BUS.register(PlayerBonusRegenHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(DifficultyHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(BlightHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(PetEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(DamageScaling.INSTANCE);

        // Morpheus compat
        if (Loader.isModLoaded("morpheus") && Config.MORPHEUS_OVERRIDE)
            SHMorpheusCompat.init();
    }

    @Override
    public void init(SRegistry registry, FMLInitializationEvent event) {
        DifficultyHandler.INSTANCE.initPotionMap();
        Config.INSTANCE.save();
    }

    @Override
    public void postInit(SRegistry registry, FMLPostInitializationEvent event) {
    }

    public void spawnParticles(EnumModParticles type, Color color, World world, double x, double y,
                               double z, double motionX, double motionY, double motionZ) {
    }

    public void playSoundOnClient(EntityPlayer player, SoundEvent sound, float volume, float pitch) {
        if (player instanceof EntityPlayerMP) {
            NetworkHandler.INSTANCE.sendTo(new MessagePlaySound(sound, volume, pitch), (EntityPlayerMP) player);
        }
    }

    public EntityPlayer getClientPlayer() {
        return null;
    }

    public int getParticleSettings() {
        return 0;
    }
}
