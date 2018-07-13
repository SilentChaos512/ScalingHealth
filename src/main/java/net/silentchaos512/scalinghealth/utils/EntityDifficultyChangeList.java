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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.ResourceLocation;
import net.silentchaos512.scalinghealth.config.Config;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Maps entities to how killing them affects difficulty.
 */
public class EntityDifficultyChangeList {
    private Map<String, DifficultyChanges> map = new HashMap<>();

    /**
     * Gets an object containing difficulty value changes for the entity
     *
     * @param entity The Entity
     * @return A {@link DifficultyChanges} object specific to {@code entity}, or a default one if
     * {@code entity} is not mapped.
     */
    @Nonnull
    public DifficultyChanges get(Entity entity) {
        ResourceLocation resource = EntityList.getKey(entity);
        if (resource == null) {
            return defaultValues(entity);
        }
        String id = resource.toString();
        String idOld = EntityList.getEntityString(entity);

        for (Entry<String, DifficultyChanges> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key.equalsIgnoreCase(id) || key.equalsIgnoreCase(idOld) || key.equalsIgnoreCase("minecraft:" + id)) {
                return entry.getValue();
            }
        }
        return defaultValues(entity);
    }

    public void put(String entityId, float onStandardKill, float onBlightKill) {
        map.put(entityId, new DifficultyChanges(onStandardKill, onBlightKill));
    }

    public void clear() {
        map.clear();
    }

    /**
     * Gets an object with default difficulty changes for the entity
     *
     * @param entity The Entity
     * @return The default {@link DifficultyChanges} object
     */
    public DifficultyChanges defaultValues(Entity entity) {
        boolean isBoss = !entity.isNonBoss();
        return new DifficultyChanges(
                // Standard kill values. Varies for bosses, hostiles, and passives.
                isBoss ? Config.DIFFICULTY_PER_BOSS_KILL
                        : entity instanceof IMob ? Config.DIFFICULTY_PER_KILL
                        : Config.DIFFICULTY_PER_PASSIVE_KILL,
                // Blight kill values. Blight bosses add both the blight and boss values.
                Config.DIFFICULTY_PER_BLIGHT_KILL
                        + (isBoss ? Config.DIFFICULTY_PER_BOSS_KILL : 0));
    }

    /**
     * Holds the amount difficulty will change by when an entity is killed. Has a value for both
     * standard and blight mobs.
     */
    public static class DifficultyChanges {
        public final float onStandardKill;
        public final float onBlightKill;

        public DifficultyChanges(float standard, float blight) {
            this.onStandardKill = standard;
            this.onBlightKill = blight;
        }
    }
}
