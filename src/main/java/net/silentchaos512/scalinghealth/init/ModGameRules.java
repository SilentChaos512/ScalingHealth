/*
package net.silentchaos512.scalinghealth.init;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.ScalingHealth;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;
// FIXME: Game rules seem to not work at all...
public enum ModGameRules implements IStringSerializable {
    DIFFICULTY(() -> "true");

    private final Supplier<String> defaultValue;

    ModGameRules(Supplier<String> defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean getBoolean(World world) {
        //return world.getGameRules().getBoolean(getName());
         return true;
    }

    @Override
    public String getName() {
        return ScalingHealth.RESOURCE_PREFIX + StringUtils.lowerCase(name());
    }

    public static void registerDefinitions() {
        for (ModGameRules rule : values()) {
            GameRules.register("shDifficulty", GameRules.BooleanValue)
            //GameRules.getDefinitions().put(rule.getName(), new GameRules.ValueDefinition(rule.defaultValue.get(), GameRules.ValueType.BOOLEAN_VALUE));
        }
    }

    public static void setRules(MinecraftServer server) {
        for (ModGameRules rule : values()) {
            //server.getGameRules().setOrCreateGameRule(rule.getName(), rule.defaultValue.get(), server);
        }
    }
}*/
