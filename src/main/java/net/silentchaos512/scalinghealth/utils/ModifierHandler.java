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

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.lib.util.EntityHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;

import java.util.List;
import java.util.UUID;

public final class ModifierHandler {
    private static final UUID MODIFIER_ID_HEALTH = UUID.fromString("c0bef565-35f6-4dc5-bb4c-3644c382e6ce");
    private static final UUID MODIFIER_ID_DAMAGE = UUID.fromString("d3560b15-c459-451c-86a8-0247015ae899");
    private static final String MODIFIER_NAME_HEALTH = "ScalingHealth.HealthModifier";
    private static final String MODIFIER_NAME_DAMAGE = "ScalingHealth.DamageModifier";

    private ModifierHandler() {throw new IllegalAccessError("Utility class");}

    public static void setModifier(LivingEntity entity, IAttribute attribute, UUID uuid, String name, double amount, AttributeModifier.Operation op) {
        IAttributeInstance instance = entity.getAttribute(attribute);
        //noinspection ConstantConditions -- instance CAN be null!
        if (instance == null) return;
        AttributeModifier mod = instance.getModifier(uuid);
        if (mod != null) instance.removeModifier(mod);
        instance.applyModifier(new AttributeModifier(uuid, name, amount, op));
    }

    public static void setMaxHealth(LivingEntity entity, double amount, AttributeModifier.Operation op) {
        double oldMax = entity.getMaxHealth();
        setModifier(entity, SharedMonsterAttributes.MAX_HEALTH, MODIFIER_ID_HEALTH, MODIFIER_NAME_HEALTH, amount, op);
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
        boolean stop = false;
        if(entity instanceof MobEntity){
            List<? extends String> mods = SHDifficulty.getDamageBlacklistedMods(entity.world);
            for (String mod : mods) {
                if (mod.equals(ForgeRegistries.ENTITIES.getKey(entity.getType()).getNamespace())) stop = true;
            }
        }
        if(stop) return;
        setModifier(entity, SharedMonsterAttributes.ATTACK_DAMAGE, MODIFIER_ID_DAMAGE, MODIFIER_NAME_DAMAGE, amount, op);
    }
}
