package net.silentchaos512.scalinghealth.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.silentchaos512.scalinghealth.ScalingHealth;

public class EntityBlightFire extends Entity implements IEntityAdditionalSpawnData {

  private EntityLivingBase parent;

  public EntityBlightFire(World worldIn) {

    super(worldIn);
  }

  public EntityBlightFire(EntityLivingBase parent) {

    super(parent.worldObj);
    this.parent = parent;
    this.startRiding(parent, true);
  }

  @Override
  public void onUpdate() {

    if (!worldObj.isRemote && (parent == null || parent.isDead)) {
      setDead();
      ScalingHealth.logHelper.debug("dead fire", FMLCommonHandler.instance().getEffectiveSide());
      return;
    }

    if (parent != null) {
      this.posX = parent.posX;
      this.posY = parent.posY + parent.height;
      this.posZ = parent.posZ;
    }

    // ScalingHealth.logHelper.debug(posX, posY, posZ);
  }

  @Override
  protected void entityInit() {

    // TODO Auto-generated method stub

  }

  @Override
  protected void readEntityFromNBT(NBTTagCompound compound) {

    // TODO Auto-generated method stub

  }

  @Override
  protected void writeEntityToNBT(NBTTagCompound compound) {

    // TODO Auto-generated method stub

  }

  public EntityLivingBase getParent() {

    return parent;
  }

  @Override
  public void writeSpawnData(ByteBuf buffer) {

    buffer.writeInt(parent == null ? -1 : parent.getEntityId());
  }

  @Override
  public void readSpawnData(ByteBuf additionalData) {

    int id = additionalData.readInt();
    if (id != -1) {
      Entity entity = worldObj.getEntityByID(id);
      if (entity instanceof EntityLivingBase)
        parent = (EntityLivingBase) entity;
    }
  }
}
