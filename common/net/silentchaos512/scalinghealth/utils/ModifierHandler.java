package net.silentchaos512.scalinghealth.utils;

import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.silentchaos512.scalinghealth.ScalingHealth;

public class ModifierHandler {

  public static final UUID MODIFIER_ID_HEALTH = UUID
      .fromString("c0bef565-35f6-4dc5-bb4c-3644c382e6ce");
  public static final UUID MODIFIER_ID_DAMAGE = UUID
      .fromString("d3560b15-c459-451c-86a8-0247015ae899");
  public static final String MODIFIER_NAME_HEALTH = ScalingHealth.MOD_ID + ".HealthModifier";
  public static final String MODIFIER_NAME_DAMAGE = ScalingHealth.MOD_ID + ".DamageModifier";

  private static void setModifier(IAttributeInstance attr, UUID id, String name, float amount) {

    if (attr == null)
      return;

    // Calculate the difference for the modifier.
    float normalValue = (float) attr.getBaseValue();
    float difference = amount - normalValue;

    // Get current and new modifier.
    AttributeModifier mod = attr.getModifier(id);
    AttributeModifier newMod = new AttributeModifier(id, name, difference, 0);

    // Remove the old, apply the new.
    if (mod != null)
      attr.removeModifier(mod);
    attr.applyModifier(newMod);
  }

  public static void setMaxHealth(EntityLivingBase entity, float amount) {

    float originalHealth = entity.getHealth();
    IAttributeInstance attr = entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
    setModifier(attr, MODIFIER_ID_HEALTH, MODIFIER_NAME_HEALTH, amount);
    entity.setHealth(originalHealth);
  }

  public static void setAttackDamage(EntityLivingBase entity, float amount) {

    IAttributeInstance attr = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
    setModifier(attr, MODIFIER_ID_DAMAGE, MODIFIER_NAME_DAMAGE, amount);
  }
}
