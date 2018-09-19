package net.silentchaos512.scalinghealth.network.message;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.silentchaos512.lib.event.ClientTicks;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.network.Message;

import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class MessagePlaySound extends Message {
    public String soundId;
    public float volume;
    public float pitch;

    @SuppressWarnings("unused")
    public MessagePlaySound() {
    }

    public MessagePlaySound(SoundEvent sound, float volume, float pitch) {
        this.soundId = Objects.requireNonNull(sound.getRegistryName()).toString();
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage handleMessage(MessageContext ctx) {
        ClientTicks.scheduleAction(() -> {
            EntityPlayer player = ScalingHealth.proxy.getClientPlayer();
            if (player != null) {
                SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(soundId));
                if (sound != null) {
                    player.playSound(sound, volume, pitch);
                }
            }
        });

        return null;
    }

}
