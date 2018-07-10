package net.silentchaos512.scalinghealth.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.silentchaos512.scalinghealth.config.Config;

public class ScalingHealthSavedData extends WorldSavedData {

  private static final String DATA_NAME = "ScalingHealth_Difficulty";
  private static final String NBT_DIFFICULTY = "Difficulty";

  public double difficulty = -1;

  public ScalingHealthSavedData() {

    super(DATA_NAME);
  }

  public ScalingHealthSavedData(String s) {

    super(s);
  }

  @Override
  public void readFromNBT(NBTTagCompound nbt) {

    difficulty = nbt.getDouble(NBT_DIFFICULTY);
    difficulty = MathHelper.clamp(difficulty, Config.DIFFICULTY_MIN,
        Config.DIFFICULTY_MAX);
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

    difficulty = MathHelper.clamp(difficulty, Config.DIFFICULTY_MIN,
        Config.DIFFICULTY_MAX);
    nbt.setDouble(NBT_DIFFICULTY, difficulty);
    return nbt;
  }

  public static ScalingHealthSavedData get(World world) {

    MapStorage storage = world.getPerWorldStorage();
    ScalingHealthSavedData instance = (ScalingHealthSavedData) storage
        .getOrLoadData(ScalingHealthSavedData.class, DATA_NAME);

    if (instance == null) {
      instance = new ScalingHealthSavedData();
      storage.setData(DATA_NAME, instance);
    }
    return instance;
  }
}
