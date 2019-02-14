package net.silentchaos512.scalinghealth.config;

import com.google.common.collect.ImmutableList;
import net.silentchaos512.lib.util.Color;
import net.silentchaos512.utils.Lazy;
import net.silentchaos512.utils.config.ConfigSpecWrapper;
import net.silentchaos512.utils.config.ConfigValue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A list of colors loaded from a config file. Uses Silent Lib's Color class.
 */
public class ColorList {
    private final Lazy<List<Color>> list;

    ColorList(ConfigSpecWrapper wrapper, String path, String comment, int... defaults) {
        // Default list of formatted values
        List<String> defaultList = Arrays
                .stream(defaults)
                .mapToObj(Color::format)
                .collect(Collectors.toList());

        // Load a string list
        ConfigValue<List<? extends String>> config = wrapper
                .builder(path)
                .comment(comment)
                .defineList(defaultList, o -> o instanceof String && Color.validate((String) o));

        // Lazy so we only need to parse once
        // TODO: What about config reloads?
        list = Lazy.of(() -> ImmutableList.copyOf(
                config.get()
                        .stream()
                        .map(Color::parse)
                        .collect(Collectors.toList())));
    }

    public List<Color> get() {
        return list.get();
    }
}
