package net.silentchaos512.scalinghealth.command;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class ModCommands {
    private ModCommands() {throw new IllegalAccessError("Utility class");}

    public static void registerAll(RegisterCommandsEvent event) {
        DifficultyCommand.register(event.getDispatcher(), event.getBuildContext());
        HealthCommand.register(event.getDispatcher(), event.getBuildContext());
        PowerCommand.register(event.getDispatcher(), event.getBuildContext());
        RecalculateCommand.register(event.getDispatcher(), event.getBuildContext());
        SummonCommand.register(event.getDispatcher(), event.getBuildContext());
    }

    static MutableComponent playerNameText(Player player) {
        return Component.translatable("command.scalinghealth.playerName",
                player.getName().copy().withStyle(ChatFormatting.ITALIC)).withStyle(ChatFormatting.AQUA);
    }

    static MutableComponent valueText(double value, double maxValue) {
        return Component.translatable("command.scalinghealth.valueOverMax",
                String.format("%.1f", value),
                String.format("%.1f", maxValue)
        ).copy().withStyle(ChatFormatting.WHITE);
    }
}
