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
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.init.ModEntities;

import javax.annotation.Nullable;

public class BlightFireEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final String NBT_PARENT = "ParentBlight";

    private MobEntity parent;

    public BlightFireEntity(World worldIn) {
        super(ModEntities.BLIGHT_FIRE.type(), worldIn);
    }

    public BlightFireEntity(MobEntity parent) {
        this(parent.world);
        this.parent = parent;
    }

    @Override
    protected void registerData() { }

    @Override
    public void tick() {
        // Server side only, blight fire must have a parent.
        if (!world.isRemote && (parent == null || !parent.isAlive())) {
            if (ScalingHealth.LOGGER.isDebugEnabled()) {
                ScalingHealth.LOGGER.debug("Removed blight fire (parent missing or dead)");
            }
            remove();
            return;
        }

        // Update position manually in case fire is not riding the blight.
        if (parent != null) {
            this.posX = parent.posX;
            this.posY = parent.posY + parent.getHeight() / 1.5;
            this.posZ = parent.posZ;
        }
    }

    @Override
    public int getBrightnessForRender() {
        return 15728880;
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        if (compound.contains(NBT_PARENT)) {
            int id = compound.getInt(NBT_PARENT);
            Entity entity = world.getEntityByID(id);
            if (entity instanceof MobEntity)
                parent = (MobEntity) entity;
        }
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        if (parent != null) {
            compound.putInt(NBT_PARENT, parent.getEntityId());
        }
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return new SSpawnObjectPacket(this);
    }

    @Nullable
    public MobEntity getParent() {
        return parent;
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(parent == null ? -1 : parent.getEntityId());
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        int id = additionalData.readInt();
        if (id != -1) {
            Entity entity = world.getEntityByID(id);
            if (entity instanceof MobEntity)
                parent = (MobEntity) entity;
        }
    }
}
