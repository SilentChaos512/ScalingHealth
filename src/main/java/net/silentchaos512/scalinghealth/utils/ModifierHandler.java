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

package net.silentchaos512.scalinghealth.utils;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.silentchaos512.lib.util.EntityHelper;

import java.util.UUID;

public final class ModifierHandler {
    private static final UUID MODIFIER_ID_HEALTH = UUID.fromString("c0bef565-35f6-4dc5-bb4c-3644c382e6ce");
    private static final UUID MODIFIER_ID_DAMAGE = UUID.fromString("d3560b15-c459-451c-86a8-0247015ae899");
    private static final String MODIFIER_NAME_HEALTH = "ScalingHealth.HealthModifier";
    private static final String MODIFIER_NAME_DAMAGE = "ScalingHealth.DamageModifier";

    private ModifierHandler() { throw new IllegalAccessError("Utility class"); }

    public static void setModifier(LivingEntity entity, Attribute attribute, UUID uuid, String name, double amount, AttributeModifier.Operation op) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return;
        AttributeModifier mod = instance.getModifier(uuid);
        if (mod != null) instance.removeModifier(mod);
        instance.addPermanentModifier(new AttributeModifier(uuid, name, amount, op));
    }

    public static void setMaxHealth(LivingEntity entity, double amount, AttributeModifier.Operation op) {
        double oldMax = entity.getMaxHealth();
        setModifier(entity, Attributes.MAX_HEALTH, MODIFIER_ID_HEALTH, MODIFIER_NAME_HEALTH, amount, op);
        double newMax = entity.getMaxHealth();

        // Heal entity when increasing max health
        if (newMax > oldMax) {
            float healAmount = (float) (newMax - oldMax);
            EntityHelper.heal(entity, healAmount, false);
        } else if (entity.getHealth() > newMax) {
            entity.setHealth((float) newMax);
        }
    }

    public static void addAttackDamage(LivingEntity entity, double amount, AttributeModifier.Operation op) {
        setModifier(entity, Attributes.ATTACK_DAMAGE, MODIFIER_ID_DAMAGE, MODIFIER_NAME_DAMAGE, amount, op);
    }
}
