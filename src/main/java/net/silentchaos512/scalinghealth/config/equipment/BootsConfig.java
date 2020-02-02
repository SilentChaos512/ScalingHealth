package net.silentchaos512.scalinghealth.config.equipment;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigSpec;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.scalinghealth.utils.EquipmentTierMap;
import net.silentchaos512.utils.Lazy;
import net.silentchaos512.utils.config.ConfigSpecWrapper;
import net.silentchaos512.utils.config.ConfigValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HelmetConfig {
    Lazy<EquipmentTierMap> equipments;

    /**
     * @param path              the path of the config (mob.equipment.ARMORTYPE)
     * @param defaultSettings   list of the commented configs - the first entry HAS to be the return:  fromGeneral
     * @return                  an instance
     */
    public static HelmetConfig init(ConfigSpecWrapper wrapper, String path, boolean includeCost, List<CommentedConfig> defaultSettings){
        ConfigSpec spec = new ConfigSpec();

        List<String> enchantments = new ArrayList<>();
        ForgeRegistries.ENCHANTMENTS.getEntries().forEach(s -> enchantments.add(s.getValue().getName()));
        spec.defineInList("enchantments", "minecraft:unknown", enchantments);
        spec.defineInRange("minDifficulty", 10.0, 0.0, Double.MAX_VALUE);
        spec.define("equipment", "minecraft:unknown", ConfigValue.IS_NONEMPTY_STRING);
        spec.defineInRange("tier", 1, 1, Integer.MAX_VALUE);

        ConfigValue<List<? extends CommentedConfig>> helmetConfig = wrapper
                .builder(path)
                .comment("The first section initializes the equipment type",
                        "Each other section is a piece of equipment with it's own enchantment possibilities, cost and tier")
                .defineList(defaultSettings, o -> {
                    if (!(o instanceof CommentedConfig)) return false;
                    return spec.isCorrect((CommentedConfig) o);
                });
        HelmetConfig result = new HelmetConfig();

        result.equipments = Lazy.of(() -> {
            EquipmentTierMap map = new EquipmentTierMap(EquipmentSlotType.HEAD);
            for(int i = 0; i < helmetConfig.get().size(); i++){
                map.put(EquipmentTierMap.EquipmentEntry.from(helmetConfig.get().get(i), true));
            }
            return map;
        });
        return result;
    }

    //MobPotionConfig's from isn't public but works... no idea why
    public static CommentedConfig from(Item equipment, int tier, List<String> enchantments, int cost) {
        CommentedConfig config = CommentedConfig.inMemory();
        config.set("equipment", Objects.requireNonNull(equipment.getRegistryName()).toString());
        config.set("tier", tier);
        config.set("enchantments", enchantments);
        config.set("minDifficulty", cost);
        return config;
    }

    public void processMob(MobEntity mob, int tier){
        equipments.get().equip(mob, tier);
    }

    public int getMaxTier(){
        return equipments.get().maxTier;
    }
}
