package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public final class ModCommands {
    private ModCommands() {throw new IllegalAccessError("Utility class");}

    public static void registerAll(CommandDispatcher<CommandSource> dispatcher) {
        DifficultyCommand.register(dispatcher);
        HealthCommand.register(dispatcher);
        PowerCommand.register(dispatcher);
        RecalculateCommand.register(dispatcher);
    }

    static ITextComponent playerNameText(EntityPlayer player) {
        ITextComponent textDim = new TextComponentTranslation("command.scalinghealth.playerName.inDimension",
                player.dimension
        ).applyTextStyle(TextFormatting.GRAY);
        return new TextComponentTranslation("command.scalinghealth.playerName",
                player.getName().applyTextStyle(TextFormatting.ITALIC),
                textDim
        ).applyTextStyle(TextFormatting.AQUA);
    }

    static ITextComponent valueText(double value, double maxValue) {
        return new TextComponentTranslation("command.scalinghealth.valueOverMax",
                String.format("%.1f", value),
                String.format("%.1f", maxValue)
        ).applyTextStyle(TextFormatting.WHITE);
    }
}
