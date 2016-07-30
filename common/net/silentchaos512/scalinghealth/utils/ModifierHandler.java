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

  public static void setMaxHealth(EntityLivingBase entity, float amount) {

    IAttributeInstance attr = entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
    if (attr != null) {
      float normalMax = (float) attr.getBaseValue();
      float difference = amount - normalMax;

      AttributeModifier mod = attr.getModifier(MODIFIER_ID_HEALTH);
      AttributeModifier newMod = new AttributeModifier(MODIFIER_ID_HEALTH, MODIFIER_NAME_HEALTH,
          difference, 0);

      // ScalingHealth.logHelper.debug(amount, normalMax, difference, mod == null);

      if (mod != null)
        attr.removeModifier(mod);
      attr.applyModifier(newMod);
    }
  }

  public static void setAttackDamage(EntityLivingBase entity, float amount) {

    IAttributeInstance attr = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
    if (attr != null) {
      float normalMax = (float) attr.getBaseValue();
      float difference = amount - normalMax;

      AttributeModifier mod = attr.getModifier(MODIFIER_ID_DAMAGE);
      AttributeModifier newMod = new AttributeModifier(MODIFIER_ID_DAMAGE, MODIFIER_NAME_DAMAGE,
          difference, 0);

      if (mod != null)
        attr.removeModifier(mod);
      attr.applyModifier(newMod);
    }
  }
}
