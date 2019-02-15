package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class ModCommands {
    public static void registerAll(CommandDispatcher<CommandSource> dispatcher) {
        DifficultyCommand.register(dispatcher);
        HealthCommand.register(dispatcher);
        RecalculateCommand.register(dispatcher);
    }

    static ITextComponent playerNameText(EntityPlayer player) {
        return new TextComponentTranslation("command.scalinghealth.playerName", player.getName(), player.dimension);
    }
}
