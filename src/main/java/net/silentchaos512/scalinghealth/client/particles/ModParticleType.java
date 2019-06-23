package net.silentchaos512.scalinghealth.client.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;

public class ModParticleType extends ParticleType<ModParticleType> implements IParticleData {
    private static final IParticleData.IDeserializer<ModParticleType> DESERIALIZER = new IDeserializer<ModParticleType>() {
        @Override
        public ModParticleType deserialize(ParticleType<ModParticleType> particleTypeIn, StringReader reader) throws CommandSyntaxException {
            return (ModParticleType) particleTypeIn;
        }

        @Override
        public ModParticleType read(ParticleType<ModParticleType> particleTypeIn, PacketBuffer buffer) {
            return (ModParticleType) particleTypeIn;
        }
    };

    ModParticleType(boolean alwaysRender) {
        super(alwaysRender, DESERIALIZER);
    }

    @Override
    public ParticleType<?> getType() {
        return this;
    }

    @Override
    public void write(PacketBuffer buffer) {}

    @Override
    public String getParameters() {
        return "";
    }
}
