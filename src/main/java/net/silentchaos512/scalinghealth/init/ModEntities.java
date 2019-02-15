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

package net.silentchaos512.scalinghealth.init;

import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.entity.EntityBlightFire;
import net.silentchaos512.utils.Lazy;

public final class ModEntities {
    public static final Lazy<EntityType<EntityBlightFire>> BLIGHT_FIRE = Lazy.of(() ->
            EntityType.Builder.create(EntityBlightFire.class, EntityBlightFire::new)
                    .build(ScalingHealth.RESOURCE_PREFIX + "blight_fire"));

    private ModEntities() {}

    public static void registerAll(RegistryEvent.Register<EntityType<?>> event) {
        // Workaround for Forge event bus bug
        if (!event.getName().equals(ForgeRegistries.ENTITIES.getRegistryName())) return;

        register("blight_fire", BLIGHT_FIRE.get());
    }

    private static void register(String name, EntityType<?> entityType) {
        ResourceLocation id = new ResourceLocation(ScalingHealth.MOD_ID, name);
        entityType.setRegistryName(id);
        ForgeRegistries.ENTITIES.register(entityType);
    }
}
