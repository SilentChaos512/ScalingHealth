/*
 * Scaling Health -- PropertyDifficulty
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

package net.silentchaos512.scalinghealth.loot.properties;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.properties.EntityProperty;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.api.ScalingHealthAPI;
import net.silentchaos512.scalinghealth.config.Config;

import java.util.Random;

public class PropertyDifficulty implements EntityProperty {
    private final int min;
    private final int max;

    private PropertyDifficulty(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean testProperty(Random random, Entity entityIn) {
        if ( entityIn instanceof EntityLivingBase) {
            double difficulty = ScalingHealthAPI.getEntityDifficulty((EntityLivingBase) entityIn);
            difficulty /= Config.Mob.Blight.difficultyMultiplier;
            return difficulty >= this.min && difficulty <= this.max;
        }
        return false;
    }

    public static class Serializer extends EntityProperty.Serializer<PropertyDifficulty> {
        public Serializer() {
            super(new ResourceLocation(ScalingHealth.MOD_ID_LOWER, "difficulty"), PropertyDifficulty.class);
        }

        @Override
        public JsonElement serialize(PropertyDifficulty property, JsonSerializationContext serializationContext) {
            JsonObject json = new JsonObject();
            json.addProperty("min", property.min);
            json.addProperty("max", property.max);
            return json;
        }

        @Override
        public PropertyDifficulty deserialize(JsonElement element, JsonDeserializationContext deserializationContext) {
            JsonObject json = element.getAsJsonObject();
            int min = JsonUtils.getInt(json, "min", 0);
            int max = JsonUtils.getInt(json, "max", Integer.MAX_VALUE);
            return new PropertyDifficulty(min, max);
        }
    }
}
