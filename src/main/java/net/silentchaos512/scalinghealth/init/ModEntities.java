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

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.client.render.entity.BlightFireRenderer;
import net.silentchaos512.scalinghealth.entity.BlightFireEntity;
import net.silentchaos512.utils.Lazy;

import java.util.Locale;
import java.util.function.Supplier;

public enum ModEntities {
    BLIGHT_FIRE(() -> EntityType.Builder.create((type, world) -> new BlightFireEntity(world), EntityClassification.AMBIENT));

    private final Lazy<EntityType<?>> type;

    ModEntities(Supplier<EntityType.Builder<?>> factory) {
        this.type = Lazy.of(() -> {
            ResourceLocation id = ScalingHealth.getId(this.getName());
            return factory.get().build(id.toString());
        });
    }

    public EntityType<?> type() {
        return type.get();
    }

    private String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static void registerAll(RegistryEvent.Register<EntityType<?>> event) {
        for (ModEntities entity : values()) {
            register(entity.getName(), entity.type());
        }
    }

    public static void registerRenderers(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(BlightFireEntity.class, new BlightFireRenderer.Factory());
    }

    private static void register(String name, EntityType<?> entityType) {
        ResourceLocation id = new ResourceLocation(ScalingHealth.MOD_ID, name);
        entityType.setRegistryName(id);
        ForgeRegistries.ENTITIES.register(entityType);
    }
}
