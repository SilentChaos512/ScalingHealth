package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.*;
import net.silentchaos512.scalinghealth.capability.DifficultySourceCapability;
import net.silentchaos512.scalinghealth.capability.IDifficultySource;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

public final class DifficultyCommand {
    private DifficultyCommand() {}

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("sh_difficulty").requires(source ->
                source.hasPermissionLevel(2));

        // get
        builder
                .then(Commands.literal("get")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(
                                        // Run for all targets
                                        DifficultyCommand::runGetDifficulty
                                )
                        )
                        .then(Commands.literal("server")
                                .executes(
                                        DifficultyCommand::runGetServerDifficulty
                                )
                        )
                        .executes(context -> {
                            // No target, use sender
                            return getDifficultySingle(context, context.getSource().asPlayer());
                        })
                );
        // set
        builder
                .then(Commands.literal("set")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                        .executes(
                                                DifficultyCommand::runSetDifficulty
                                        )
                                )
                        )
                        .then(Commands.literal("server")
                                .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                        .executes(
                                                DifficultyCommand::runSetServerDifficulty
                                        )
                                )
                        )
                );
        // add
        builder
                .then(Commands.literal("add")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                        .executes(
                                                DifficultyCommand::runAddDifficulty
                                        )
                                )
                        )
                        .then(Commands.literal("server")
                                .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                        .executes(
                                                DifficultyCommand::runAddServerDifficulty
                                        )
                                )
                        )
                );
        dispatcher.register(builder);
    }

    private static int runGetDifficulty(CommandContext<CommandSource> context) throws CommandSyntaxException {
        for (ServerPlayerEntity player : EntityArgument.getPlayers(context, "targets")) {
            getDifficultySingle(context, player);
        }
        return 1;
    }

    private static int getDifficultySingle(CommandContext<CommandSource> context, PlayerEntity player) {
        IDifficultySource source = SHDifficulty.source(player);
        context.getSource().sendFeedback(ModCommands.playerNameText(player), true);
        double maxDifficulty = SHDifficulty.maxValue();

        // Player difficulty
        float difficulty = source.getDifficulty();
        IFormattableTextComponent playerValues = ModCommands.valueText(difficulty, maxDifficulty);
        IFormattableTextComponent playerText = text("player", playerValues)
                .mergeStyle(TextFormatting.YELLOW);
        context.getSource().sendFeedback(playerText, true);

        // Area difficulty
        double areaDifficulty = SHDifficulty.areaDifficulty(player.world, player.getPosition());
        IFormattableTextComponent areaValues = ModCommands.valueText(areaDifficulty, maxDifficulty);
        IFormattableTextComponent areaText = text("area", areaValues)
                .mergeStyle(TextFormatting.YELLOW);

        // Area mode
        ITextComponent modeText = new StringTextComponent(" (")
                .appendSibling(new TranslationTextComponent("scalinghealth.modes.difficulty." + SHDifficulty.areaMode().getName()).mergeStyle(TextFormatting.GRAY))
                .appendString(")");
        areaText.appendSibling(modeText);
        context.getSource().sendFeedback(areaText, true);
        return 1;
    }

    private static int runGetServerDifficulty(CommandContext<CommandSource> context) {
        IDifficultySource source = DifficultySourceCapability.getOverworldCap().orElseGet(DifficultySourceCapability::new);

        // Difficulty
        double difficulty = source.getDifficulty();
        double maxDifficulty = SHDifficulty.maxValue();
        IFormattableTextComponent textValues = ModCommands.valueText(difficulty, maxDifficulty);
        IFormattableTextComponent textDifficulty = text("server", textValues)
                .mergeStyle(TextFormatting.YELLOW);
        context.getSource().sendFeedback(textDifficulty, true);
        return 1;
    }

    private static int runSetDifficulty(CommandContext<CommandSource> context) throws CommandSyntaxException {
        float amount = FloatArgumentType.getFloat(context, "amount");
        for (ServerPlayerEntity player : EntityArgument.getPlayers(context, "targets")) {
            SHDifficulty.source(player).setDifficulty(amount);
        }
        return 1;
    }

    private static int runSetServerDifficulty(CommandContext<CommandSource> context) {
        float amount = FloatArgumentType.getFloat(context, "amount");
        DifficultySourceCapability.getOverworldCap().orElseGet(DifficultySourceCapability::new).setDifficulty(amount);
        return 1;
    }

    private static int runAddDifficulty(CommandContext<CommandSource> context) throws CommandSyntaxException {
        float amount = FloatArgumentType.getFloat(context, "amount");
        for (ServerPlayerEntity player : EntityArgument.getPlayers(context, "targets")) {
            SHDifficulty.source(player).addDifficulty(amount);
        }
        return 1;
    }

    private static int runAddServerDifficulty(CommandContext<CommandSource> context) {
        float amount = FloatArgumentType.getFloat(context, "amount");
        DifficultySourceCapability.getOverworldCap().orElseGet(DifficultySourceCapability::new).addDifficulty(amount);
        return 1;
    }

    private static IFormattableTextComponent text(String key, Object... args) {
        return new TranslationTextComponent("command.scalinghealth.difficulty." + key, args);
    }
}
