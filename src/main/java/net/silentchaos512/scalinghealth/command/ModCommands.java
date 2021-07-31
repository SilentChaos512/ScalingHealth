package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

public final class ModCommands {
    private ModCommands() {throw new IllegalAccessError("Utility class");}

    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher) {
        DifficultyCommand.register(dispatcher);
        HealthCommand.register(dispatcher);
        PowerCommand.register(dispatcher);
        RecalculateCommand.register(dispatcher);
        SummonCommand.register(dispatcher);
    }

    static MutableComponent playerNameText(Player player) {
        return new TranslatableComponent("command.scalinghealth.playerName",
                player.getName().copy().withStyle(ChatFormatting.ITALIC)).withStyle(ChatFormatting.AQUA);
    }

    static MutableComponent valueText(double value, double maxValue) {
        return new TranslatableComponent("command.scalinghealth.valueOverMax",
                String.format("%.1f", value),
                String.format("%.1f", maxValue)
        ).copy().withStyle(ChatFormatting.WHITE);
    }
}
