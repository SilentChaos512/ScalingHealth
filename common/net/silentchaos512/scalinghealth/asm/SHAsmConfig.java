package net.silentchaos512.scalinghealth.asm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

public class SHAsmConfig {

  public static final String PATH = "./config/scalinghealth/asm.cfg";
  public static final String MAX_HEALTH_MAX = "SharedMonsterAttributes.MAX_HEALTH:maxValue";

  static Map<String, Double> map = Maps.newHashMap();

  public static void init() {

    map.put(MAX_HEALTH_MAX, 1024D);
  }

  public static void load() {

    Path path = FileSystems.getDefault().getPath(PATH);
    File file = new File(PATH);

    try {
      file.getParentFile().mkdirs();
      if (!file.exists())
        file.createNewFile();

      for (String line : Files.readAllLines(path)) {
        String[] array = line.split("=");
        if (array.length == 2) {
          try {
            map.put(array[0], Double.parseDouble(array[1]));
          } catch (Exception ex) {
          }
        }
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public static void save() {

    Path path = FileSystems.getDefault().getPath(PATH);
    File file = new File(PATH);

    try (FileWriter writer = new FileWriter(PATH)) {
      file.getParentFile().mkdirs();
      if (!file.exists())
        file.createNewFile();

      for (Entry<String, Double> entry : map.entrySet()) {
        writer.write(entry.getKey() + "=" + entry.getValue().toString() + "\n");
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public static double getValue(String key) {

    return map.get(key);
  }
}
