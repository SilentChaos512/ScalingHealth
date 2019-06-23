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

package net.silentchaos512.scalinghealth.lib;

import net.minecraft.entity.ai.attributes.AttributeModifier;

public enum MobHealthMode {
    ADD(AttributeModifier.Operation.ADDITION) {
        @Override
        public double getModifierValue(double baseHealthBoost, double baseMaxHealth) {
            return baseHealthBoost;
        }
    },
    MULTI(AttributeModifier.Operation.MULTIPLY_BASE) {
        @Override
        public double getModifierValue(double baseHealthBoost, double baseMaxHealth) {
            return baseHealthBoost / 20.0;
        }
    },
    MULTI_HALF(AttributeModifier.Operation.MULTIPLY_BASE) {
        @Override
        public double getModifierValue(double baseHealthBoost, double baseMaxHealth) {
            double healthScaleDiff = Math.max(0, baseMaxHealth - 20f);
            return baseHealthBoost / (20.0 + healthScaleDiff * 0.5);
        }
    },
    MULTI_QUARTER(AttributeModifier.Operation.MULTIPLY_BASE) {
        @Override
        public double getModifierValue(double baseHealthBoost, double baseMaxHealth) {
            double healthScaleDiff = Math.max(0, baseMaxHealth - 20f);
            return baseHealthBoost / (20.0 + healthScaleDiff * 0.75);
        }
    };

    public final AttributeModifier.Operation operator;

    MobHealthMode(AttributeModifier.Operation operator) {
        this.operator = operator;
    }

    public abstract double getModifierValue(double baseHealthBoost, double baseMaxHealth);

    public AttributeModifier.Operation getOperator() {
        return operator;
    }
}
