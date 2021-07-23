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

package net.silentchaos512.scalinghealth.objects.potion;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.silentchaos512.lib.util.EntityHelper;

import java.util.List;

public class BandagedEffect extends Effect {
    private static final float BASE_HEAL_RATE = 0.005f;
    public static final double SPEED_MODIFIER = -0.25;
    public static final String MOD_UUID = "732486d8-f730-41a2-868f-eb988738986f";

    public BandagedEffect(EffectType type, int color) {
        super(type, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entityLiving, int amplifier) {
        // Remove effect if fully healed.
        if (entityLiving.getHealth() >= entityLiving.getMaxHealth()) {
            entityLiving.removeEffect(this);
        }

        float healAmount = BASE_HEAL_RATE * entityLiving.getMaxHealth() * (amplifier + 1);
        // Using Entity#heal allows us to prevent the cancelable LivingHealEvent from being fired.
        EntityHelper.heal(entityLiving, healAmount, true);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Heal every second.
        return duration % 20 == 0;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        // Milk doesn't melt bandages off... right?
        return ImmutableList.of();
    }

    @Override
    public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
        // I don't want to consider the amplifier.
        return modifier.getAmount();
    }
}
