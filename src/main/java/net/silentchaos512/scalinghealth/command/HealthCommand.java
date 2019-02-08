package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;

import java.util.Locale;

public class HealthCommand {
    public enum Type {
        GET, SET, ADD;

        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("shhealth").requires(source ->
                source.hasPermissionLevel(2));

        for (Type type : Type.values()) {
            builder.then(Commands.literal(type.getName()).then(Commands.argument("targets", EntityArgument.multiplePlayers())));
        }
    }
}
