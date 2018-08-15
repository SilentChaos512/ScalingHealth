/*
 * Scaling Health -- EntityPropertyBlight
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

import com.google.gson.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.properties.EntityProperty;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.event.BlightHandler;

import java.util.Random;

public class PropertyBlight implements EntityProperty {
    private final boolean isBlight;

    private PropertyBlight(boolean isBlight) {
        this.isBlight = isBlight;
    }

    @Override
    public boolean testProperty(Random random, Entity entityIn) {
        return entityIn instanceof EntityLivingBase && this.isBlight == BlightHandler.isBlight((EntityLivingBase) entityIn);
    }

    public static class Serializer extends EntityProperty.Serializer<PropertyBlight> {
        static final ResourceLocation NAME = new ResourceLocation(ScalingHealth.MOD_ID_LOWER, "is_blight");

        public Serializer() {
            super(NAME, PropertyBlight.class);
        }

        @Override
        public JsonElement serialize(PropertyBlight property, JsonSerializationContext serializationContext) {
            return new JsonPrimitive(property.isBlight);
        }

        @Override
        public PropertyBlight deserialize(JsonElement element, JsonDeserializationContext deserializationContext) {
            return new PropertyBlight(JsonUtils.getBoolean(element, NAME.toString()));
        }
    }
}
