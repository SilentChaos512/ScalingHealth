/*
 * ScalingHealth - ModifierHandler
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.scalinghealth.utils;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;

import java.util.UUID;

public class ModifierHandler {

    public static final UUID MODIFIER_ID_HEALTH = UUID.fromString("c0bef565-35f6-4dc5-bb4c-3644c382e6ce");
    public static final UUID MODIFIER_ID_DAMAGE = UUID.fromString("d3560b15-c459-451c-86a8-0247015ae899");
    public static final String MODIFIER_NAME_HEALTH = ScalingHealth.MOD_ID_OLD + ".HealthModifier";
    public static final String MODIFIER_NAME_DAMAGE = ScalingHealth.MOD_ID_OLD + ".DamageModifier";

    private static void setModifier(IAttributeInstance attr, UUID id, String name, double amount, int op) {
        if (attr == null)
            return;

        // Calculate the difference for the modifier.
        double normalValue = attr.getBaseValue();
        double difference = amount - normalValue;

        // Get current and new modifier.
        AttributeModifier mod = attr.getModifier(id);
        AttributeModifier newMod = new AttributeModifier(id, name, difference, op);

        // Remove the old, apply the new.
        if (mod != null)
            attr.removeModifier(mod);
        attr.applyModifier(newMod);
    }

    public static void setMaxHealth(EntityLivingBase entity, double amount, int op) {
        if (amount <= 0) {
            ScalingHealth.logHelper.warning("ModifierHandler.setMaxHealth: amount <= 0!");
            return;
        }

        if (entity instanceof EntityPlayer && !Config.ALLOW_PLAYER_MODIFIED_HEALTH) {
            // It's a player, but the user has disallowed Scaling Health from modifying the player's health.
            ScalingHealth.logHelper.info(String.format(
                    "Would have set player %s's health to %.1f, but modified player health has been disabled"
                            + " in the config!",
                    entity.getName(), amount));
            return;
        }

        float originalHealth = entity.getHealth();
        IAttributeInstance attr = entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (attr != null) {
            setModifier(attr, MODIFIER_ID_HEALTH, MODIFIER_NAME_HEALTH, amount, op);
            entity.setHealth(originalHealth);
        }
    }

    public static void addAttackDamage(EntityLivingBase entity, double amount, int op) {
        IAttributeInstance attr = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (attr != null) {
            amount += attr.getBaseValue();
            setModifier(attr, MODIFIER_ID_DAMAGE, MODIFIER_NAME_DAMAGE, amount, op);
        }
    }
}
