package net.silentchaos512.scalinghealth.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.utils.ModifierHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PetHealthCapability implements IPetData, ICapabilitySerializable<CompoundTag> {
    @CapabilityInject(IPetData.class)
    public static Capability<IPetData> INSTANCE = null;
    public static ResourceLocation NAME = ScalingHealth.getId("pet_health");

    private static final String NBT_HEALTH = "SHPetBonusHealth";

    private final LazyOptional<IPetData> holder = LazyOptional.of(() -> this);

    private float bonusHealth;
    private boolean refreshed = false;

    @Override
    public void addHealth(double hp, TamableAnimal pet ) {
        bonusHealth += hp;
        ModifierHandler.setMaxHealth(pet, bonusHealth, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public float getBonusHealth() {
        return bonusHealth;
    }

    @Override
    public void tick(TamableAnimal pet) {
        if(!refreshed && pet.tickCount > 2){
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
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putFloat(NBT_HEALTH, bonusHealth);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
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
        return obj instanceof TamableAnimal;
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IPetData.class);
    }
}
