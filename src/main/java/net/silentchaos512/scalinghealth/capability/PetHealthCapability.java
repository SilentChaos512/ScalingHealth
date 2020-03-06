package net.silentchaos512.scalinghealth.capability;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.utils.ModifierHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PetHealthCapability implements IPetData, ICapabilitySerializable<CompoundNBT> {
    @CapabilityInject(IPetData.class)
    public static Capability<IPetData> INSTANCE = null;
    public static ResourceLocation NAME = ScalingHealth.getId("pet_health");

    private static final String NBT_HEALTH = "SHPetBonusHealth";

    private final LazyOptional<IPetData> holder = LazyOptional.of(() -> this);

    private float bonusHealth;
    private boolean refreshed = false;

    @Override
    public void addHealth(double hp, TameableEntity pet ) {
        bonusHealth += hp;
        ModifierHandler.setMaxHealth(pet, bonusHealth, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public float getBonusHealth() {
        return bonusHealth;
    }

    @Override
    public void tick(TameableEntity pet) {
        if(!refreshed && pet.ticksExisted > 2){
            refreshed = true;
            ModifierHandler.setMaxHealth(pet, getBonusHealth(), AttributeModifier.Operation.ADDITION);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return INSTANCE.orEmpty(cap, holder);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat(NBT_HEALTH, bonusHealth);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        bonusHealth = nbt.getFloat(NBT_HEALTH);
    }

    public static boolean canAttachTo(ICapabilityProvider obj) {
        try {
            if (obj.getCapability(INSTANCE).isPresent()) {
                return false;
            }
        } catch (NullPointerException ex) {
            ScalingHealth.LOGGER.error("Failed to get capabilities from {}", obj);
            return false;
        }
        return obj instanceof TameableEntity;
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IPetData.class, new Storage(), PetHealthCapability::new);
    }

    private static class Storage implements Capability.IStorage<IPetData> {
        @Nullable
        @Override
        public INBT writeNBT(Capability<IPetData> capability, IPetData instance, Direction side) {
            if (instance instanceof PetHealthCapability) {
                return ((PetHealthCapability) instance).serializeNBT();
            }
            return new CompoundNBT();
        }

        @Override
        public void readNBT(Capability<IPetData> capability, IPetData instance, Direction side, INBT nbt) {
            if (instance instanceof PetHealthCapability) {
                ((PetHealthCapability) instance).deserializeNBT((CompoundNBT) nbt);
            }
        }
    }
}
