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

package net.silentchaos512.scalinghealth.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.init.ModEntities;

public class BlightFireEntity extends Entity {
    public BlightFireEntity(World worldIn) {
        super(ModEntities.BLIGHT_FIRE.type(), worldIn);
    }

    public BlightFireEntity(MobEntity p) {
        this(p.world);
        this.startRiding(p);
    }

    @Override
    protected void registerData() {

    }

    @Override
    public void tick() {
        // Server side only, blight fire must have a parent.
        if(world.isRemote) return;
        Entity parent = this.getRidingEntity();
        if (parent == null || !parent.isAlive()) {
            if (ScalingHealth.LOGGER.isDebugEnabled()) {
                ScalingHealth.LOGGER.debug("Removed blight fire (parent missing or dead)");
            }
            remove();
        }
    }

    @Override
    public float getBrightness() {
        return 15728880;
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {

    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {

    }

    @Override
    public IPacket<?> createSpawnPacket() {
        ScalingHealth.LOGGER.debug("Blight Fire spawned on the SERVER");
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
