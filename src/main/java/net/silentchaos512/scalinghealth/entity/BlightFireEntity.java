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
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.init.ModEntities;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nullable;

public class BlightFireEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final Marker MARKER = MarkerManager.getMarker("BlightFireEntity");
    private static final String NBT_PARENT = "ParentBlight";

    private static final DataParameter<Integer> PARENT = EntityDataManager.createKey(BlightFireEntity.class, DataSerializers.VARINT);

    public BlightFireEntity(World worldIn) {
        super(ModEntities.BLIGHT_FIRE.type(), worldIn);
    }

    public BlightFireEntity(MobEntity parent) {
        this(parent.world);
        this.dataManager.set(PARENT, parent.getEntityId());
        this.startRiding(parent);
    }

    @Override
    protected void registerData() {
        this.dataManager.register(PARENT, 1);
    }

    @Override
    public void tick() {
        // Server side only, blight fire must have a parent.
        if(!world.isRemote){
            MobEntity p = (MobEntity) world.getEntityByID(this.dataManager.get(PARENT));
            if (p == null || !p.isAlive()) {
                if (ScalingHealth.LOGGER.isDebugEnabled()) {
                    ScalingHealth.LOGGER.debug("Removed blight fire (parent missing or dead)");
                }
                remove();
            }
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
                this.dataManager.set(PARENT, id);
        }
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        if (world.getEntityByID(this.dataManager.get(PARENT)) != null) {
            compound.putInt(NBT_PARENT, this.dataManager.get(PARENT));
        }
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        ScalingHealth.LOGGER.debug("Blight Fire spawned on the SERVER");
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Nullable
    public MobEntity getParent() {
        return (MobEntity) world.getEntityByID(this.dataManager.get(PARENT));
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(world.getEntityByID(this.dataManager.get(PARENT)) == null ? -1 : this.dataManager.get(PARENT));
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        int id = additionalData.readInt();
        if (id != -1) {
            Entity entity = world.getEntityByID(id);
            if (entity instanceof MobEntity)
                this.dataManager.set(PARENT, id);
        }
    }
}
