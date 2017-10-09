package net.silentchaos512.scalinghealth.utils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.silentchaos512.scalinghealth.ScalingHealth;

public class EntityMatchList {

  List<String> list = new ArrayList<>();

  public void add(String str) {

    list.add(str);
  }

  public void clear() {

    list.clear();
  }

  public boolean contains(Entity entity) {

    String id = EntityList.getKey(entity).getResourcePath();
    String idOld = EntityList.getEntityString(entity);

    for (String entry : list) {
      if (entry.equalsIgnoreCase(id) || entry.equalsIgnoreCase(idOld)
          || ("minecraft:" + entry).equalsIgnoreCase(id)) {
        ScalingHealth.logHelper.debug(entity.getName() + " is blacklisted!");
        return true;
      }
    }
    return false;
  }
}
