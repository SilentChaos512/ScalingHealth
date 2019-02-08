package net.silentchaos512.scalinghealth.config;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.LazyLoadBase;
import net.minecraftforge.common.ForgeConfigSpec;
import net.silentchaos512.lib.util.Color;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A list of colors loaded from a config file. Uses Silent Lib's Color class.
 */
public class ColorList {
    private final LazyLoadBase<List<Color>> list;

    ColorList(ForgeConfigSpec.Builder builder, String path, String comment, int... defaults) {
        builder.comment(comment);

        // Default list of formatted values
        List<String> defaultList = Arrays
                .stream(defaults)
                .mapToObj(Color::format)
                .collect(Collectors.toList());

        // Load a string list
        ForgeConfigSpec.ConfigValue<List<? extends String>> config = builder
                .defineList(path, defaultList, o -> o instanceof String && Color.validate((String) o));

        // Lazy load so we only need to parse once
        list = new LazyLoadBase<>(() -> ImmutableList.copyOf(
                config.get()
                        .stream()
                        .map(Color::parse)
                        .collect(Collectors.toList())));
    }

    public List<Color> get() {
        return list.getValue();
    }
}
