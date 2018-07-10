package net.silentchaos512.scalinghealth.potion;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.silentchaos512.lib.util.EntityHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;

public class PotionBandaged extends Potion {

  static final float BASE_HEAL_RATE = 0.005f;

  public PotionBandaged() {

    super(false, 0xf7dcad);
    String name = ScalingHealth.RESOURCE_PREFIX + "bandaged";
    setPotionName("effect." + name);
    setBeneficial();
    registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED,
        "732486d8-f730-41a2-868f-eb988738986f", -0.25, 2);
    setRegistryName(new ResourceLocation(name));
  }

  @Override
  public void performEffect(EntityLivingBase entityLiving, int amplifier) {

    // Remove effect if fully healed.
    if (entityLiving.getHealth() >= entityLiving.getMaxHealth()) {
      entityLiving.removePotionEffect(this);
    }

    float healAmount = BASE_HEAL_RATE * entityLiving.getMaxHealth() * (amplifier + 1);
    // Using Entity#heal allows us to prevent the cancelable LivingHealEvent from being fired.
     EntityHelper.heal(entityLiving, healAmount, false);
  }

  @Override
  public boolean isReady(int duration, int amplifier) {

    // Heal every second.
    return duration % 20 == 0;
  }

  @Override
  public List<ItemStack> getCurativeItems() {

    // Milk doesn't melt bandages off... right?
    return new ArrayList<>();
  }

  @Override
  public double getAttributeModifierAmount(int amplifier, AttributeModifier modifier) {

    // I don't want to consider the amplifier.
    return modifier.getAmount();
  }
}
