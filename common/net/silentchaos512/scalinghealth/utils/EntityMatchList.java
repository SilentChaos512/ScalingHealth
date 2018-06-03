package net.silentchaos512.scalinghealth.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;

public class EntityMatchList {

  List<String> list = new ArrayList<>();
  boolean whitelist = false;

  public void add(String str) {

    list.add(str);
  }

  @Deprecated
  public void clear() {

    list.clear();
  }

  public boolean matches(Entity entity) {

    boolean contains = this.contains(entity);
    return this.whitelist ? contains : !contains;
  }

  @Deprecated
  public boolean contains(Entity entity) {

    ResourceLocation resource = EntityList.getKey(entity);
    if (resource == null)
      return false;

    String id = resource.toString();
    String idOld = EntityList.getEntityString(entity);

    for (String entry : list)
      if (entry.equalsIgnoreCase(id) || entry.equalsIgnoreCase(idOld) || entry.equalsIgnoreCase("minecraft:" + id))
        return true;
    return false;
  }

  public void loadConfig(Configuration config, String name, String category, String[] defaults,
      boolean defaultWhitelist, String comment) {

    String nameList = name + " List";
    String nameWhitelist = name + " IsWhitelist";

    this.clear();
    for (String str : config.getStringList(nameList, category, defaults, comment))
      list.add(str);

    this.whitelist = config.getBoolean(nameWhitelist, category, defaultWhitelist,
        "If true, the list is a whitelist. Otherwise it is a blacklist.");
  }
}
