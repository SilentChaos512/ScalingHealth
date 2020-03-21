package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public final class ModCommands {
    private ModCommands() {throw new IllegalAccessError("Utility class");}

    public static void registerAll(CommandDispatcher<CommandSource> dispatcher) {
        DifficultyCommand.register(dispatcher);
        HealthCommand.register(dispatcher);
        PowerCommand.register(dispatcher);
        RecalculateCommand.register(dispatcher);
        SummonCommand.register(dispatcher);
    }

    static ITextComponent playerNameText(PlayerEntity player) {
        return new TranslationTextComponent("command.scalinghealth.playerName",
                player.getName().applyTextStyle(TextFormatting.ITALIC)).applyTextStyle(TextFormatting.AQUA);
    }

    static ITextComponent valueText(double value, double maxValue) {
        return new TranslationTextComponent("command.scalinghealth.valueOverMax",
                String.format("%.1f", value),
                String.format("%.1f", maxValue)
        ).applyTextStyle(TextFormatting.WHITE);
    }
}
