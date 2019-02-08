package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

public class ModCommands {
    public static void registerAll(CommandDispatcher<CommandSource> dispatcher) {
        HealthCommand.register(dispatcher);
    }
}
