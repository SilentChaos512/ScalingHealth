package net.silentchaos512.scalinghealth.utils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
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

    ResourceLocation resource = EntityList.getKey(entity);
    if (resource == null) {
      return false;
    }
    String id = resource.getResourcePath();
    String idOld = EntityList.getEntityString(entity);

    for (String entry : list) {
      if (entry.equalsIgnoreCase(id) || entry.equalsIgnoreCase(idOld)
          || entry.equalsIgnoreCase("minecraft:" + id)) {
        return true;
      }
    }
    return false;
  }
}
