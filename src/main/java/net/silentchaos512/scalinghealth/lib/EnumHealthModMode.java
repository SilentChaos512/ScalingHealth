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

public enum EnumHealthModMode {
    ADD(0), MULTI(1), MULTI_HALF(1), MULTI_QUARTER(1);

    public final int op;

    EnumHealthModMode(int op) {
        this.op = op;
    }

    /*
    public static EnumHealthModMode loadFromConfig(Configuration c, EnumHealthModMode defaultValue) {
        String[] validValues = new String[values().length];
        for (int i = 0; i < values().length; ++i)
            validValues[i] = values()[i].name();

        String str = c.getString("Scaling Mode", Config.CAT_MOB_HEALTH,
                defaultValue.name(),
                "Describes how extra mob health is applied. This will not change the health of mobs that already exist!\n"
                        + "  ADD - Adds a value based on difficulty to the mob's health, ignoring the mob's default health.\n"
                        + "  MULTI - Multiplies the mob's health instead of adding a flat value. For example, endermen\n"
                        + "    will always have around twice the health of zombies with this option.\n"
                        + "  MULTI_HALF - Multiplies the mob's health, but the value is reduced for higher-health mobs.\n"
                        + "  MULTI_QUARTER - Same as MULTI_HALF, but the scaling factor is even less.",
                validValues);

        for (EnumHealthModMode mode : values())
            if (mode.name().equals(str))
                return mode;
        return defaultValue;
    }
    */
}
