package net.silentchaos512.scalinghealth.init;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.ScalingHealth;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;

public enum ModGameRules implements IStringSerializable {
    DIFFICULTY(() -> "true");

    private final Supplier<String> defaultValue;

    ModGameRules(Supplier<String> defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean getBoolean(World world) {
        // FIXME: Game rules are not being registered?
//        return world.getGameRules().getBoolean(getName());
        return true;
    }

    @Override
    public String getName() {
        return ScalingHealth.RESOURCE_PREFIX + StringUtils.lowerCase(name());
    }

    public static void registerAll(MinecraftServer server) {
        for (ModGameRules rule : values()) {
            server.getGameRules().setOrCreateGameRule(rule.getName(), rule.defaultValue.get(), server);
        }
    }
}
