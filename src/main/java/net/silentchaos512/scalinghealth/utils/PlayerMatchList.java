package net.silentchaos512.scalinghealth.utils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class PlayerMatchList {

  List<String> list = new ArrayList<>();

  public void add(String name) {

    list.add(name);
  }

  public void clear() {

    list.clear();
  }

  public boolean contains(EntityPlayer player) {

    if (player == null)
      return false;

    for (String name : list)
      if (name.equalsIgnoreCase(player.getName()))
        return true;
    return false;
  }
}
