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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.entity.EntityBlightFire;

public class ModEntities {
    public static EntityType<EntityBlightFire> blightFire;

    public static void registerAll(RegistryEvent.Register<EntityType<?>> event) {
        IForgeRegistry<EntityType<?>> reg = event.getRegistry();

        blightFire = register(reg, "blight_fire", EntityType.Builder.create(EntityBlightFire.class, EntityBlightFire::new));
    }

    private static <T extends Entity> EntityType<T> register(IForgeRegistry<EntityType<?>> reg, String name, EntityType.Builder<T> builder) {
        ResourceLocation id = new ResourceLocation(ScalingHealth.MOD_ID, name);
        EntityType<T> entityType = builder.build(id.toString());
        entityType.setRegistryName(id);
        reg.register(entityType);
        return entityType;
    }
}
