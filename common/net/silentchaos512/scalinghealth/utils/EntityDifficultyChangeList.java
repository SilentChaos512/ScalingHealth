package net.silentchaos512.scalinghealth.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.ResourceLocation;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;

public class EntityDifficultyChangeList {

  Map<String, DifficultyChanges> map = new HashMap<>();

  public @Nonnull DifficultyChanges get(Entity entity) {

    ResourceLocation resource = EntityList.getKey(entity);
    if (resource == null) {
      return defaultValues(entity);
    }
    String id = resource.toString();
    String idOld = EntityList.getEntityString(entity);

    for (Entry<String, DifficultyChanges> entry : map.entrySet()) {
      String key = entry.getKey();
      if (key.equalsIgnoreCase(id) || key.equalsIgnoreCase(idOld)
          || key.equalsIgnoreCase("minecraft:" + id)) {
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

  public DifficultyChanges defaultValues(Entity entity) {

    boolean isBoss = !entity.isNonBoss();
    return new DifficultyChanges(
        // Standard kill values. Varies for bosses, hostiles, and passives.
        isBoss ? ConfigScalingHealth.DIFFICULTY_PER_BOSS_KILL
            : entity instanceof IMob ? ConfigScalingHealth.DIFFICULTY_PER_KILL
                : ConfigScalingHealth.DIFFICULTY_PER_PASSIVE_KILL,
        // Blight kill values. Blight bosses add both the blight and boss values.
        ConfigScalingHealth.DIFFICULTY_PER_BLIGHT_KILL
            + (isBoss ? ConfigScalingHealth.DIFFICULTY_PER_BOSS_KILL : 0));
  }

  public static class DifficultyChanges {

    public final float onStandardKill;
    public final float onBlightKill;

    public DifficultyChanges(float standard, float blight) {

      this.onStandardKill = standard;
      this.onBlightKill = blight;
    }
  }
}
