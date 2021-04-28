package net.silentchaos512.scalinghealth.config;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeConfigSpec;
import net.silentchaos512.utils.Color;
import net.silentchaos512.utils.Lazy;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A list of colors loaded from a config file. Uses Silent Lib's Color class.
 */
public class ColorList {
    private Lazy<List<Integer>> list;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> config;

    public ColorList(ForgeConfigSpec.Builder builder, String path, String comment, int... defaults) {
        // Default list of formatted values
        List<String> defaultList = Arrays
                .stream(defaults)
                .mapToObj(Color::format)
                .collect(Collectors.toList());

        recalculate();

        // Load a string list
        this.config = builder
                .comment(comment)
                .defineList(path, defaultList, o -> o instanceof String && Color.validate((String) o));
    }

    public void recalculate()  {
        list = Lazy.of(() -> ImmutableList.copyOf(
                config.get()
                        .stream()
                        .map(Color::parseInt)
                        .collect(Collectors.toList())));
    }

    public List<Integer> get() {
        return list.get();
    }
}
