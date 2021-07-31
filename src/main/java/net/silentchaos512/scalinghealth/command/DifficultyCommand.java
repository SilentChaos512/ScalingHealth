package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.silentchaos512.scalinghealth.capability.DifficultySourceCapability;
import net.silentchaos512.scalinghealth.capability.IDifficultySource;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

public final class DifficultyCommand {
    private DifficultyCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("sh_difficulty").requires(source ->
                source.hasPermission(2));

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
                            return getDifficultySingle(context, context.getSource().getPlayerOrException());
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

    private static int runGetDifficulty(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
            getDifficultySingle(context, player);
        }
        return 1;
    }

    private static int getDifficultySingle(CommandContext<CommandSourceStack> context, Player player) {
        IDifficultySource source = SHDifficulty.source(player);
        context.getSource().sendSuccess(ModCommands.playerNameText(player), true);
        double maxDifficulty = SHDifficulty.maxValue();

        // Player difficulty
        float difficulty = source.getDifficulty();
        MutableComponent playerValues = ModCommands.valueText(difficulty, maxDifficulty);
        MutableComponent playerText = text("player", playerValues)
                .withStyle(ChatFormatting.YELLOW);
        context.getSource().sendSuccess(playerText, true);

        // Area difficulty
        double areaDifficulty = SHDifficulty.areaDifficulty(player.level, player.blockPosition());
        MutableComponent areaValues = ModCommands.valueText(areaDifficulty, maxDifficulty);
        MutableComponent areaText = text("area", areaValues)
                .withStyle(ChatFormatting.YELLOW);

        // Area mode
        Component modeText = new TextComponent(" (")
                .append(new TranslatableComponent("scalinghealth.modes.difficulty." + SHDifficulty.areaMode().getName()).withStyle(ChatFormatting.GRAY))
                .append(")");
        areaText.append(modeText);
        context.getSource().sendSuccess(areaText, true);
        return 1;
    }

    private static int runGetServerDifficulty(CommandContext<CommandSourceStack> context) {
        IDifficultySource source = DifficultySourceCapability.getOverworldCap().orElseGet(DifficultySourceCapability::new);

        // Difficulty
        double difficulty = source.getDifficulty();
        double maxDifficulty = SHDifficulty.maxValue();
        MutableComponent textValues = ModCommands.valueText(difficulty, maxDifficulty);
        MutableComponent textDifficulty = text("server", textValues)
                .withStyle(ChatFormatting.YELLOW);
        context.getSource().sendSuccess(textDifficulty, true);
        return 1;
    }

    private static int runSetDifficulty(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        float amount = FloatArgumentType.getFloat(context, "amount");
        for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
            SHDifficulty.source(player).setDifficulty(amount);
        }
        return 1;
    }

    private static int runSetServerDifficulty(CommandContext<CommandSourceStack> context) {
        float amount = FloatArgumentType.getFloat(context, "amount");
        DifficultySourceCapability.getOverworldCap().orElseGet(DifficultySourceCapability::new).setDifficulty(amount);
        return 1;
    }

    private static int runAddDifficulty(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        float amount = FloatArgumentType.getFloat(context, "amount");
        for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
            SHDifficulty.source(player).addDifficulty(amount);
        }
        return 1;
    }

    private static int runAddServerDifficulty(CommandContext<CommandSourceStack> context) {
        float amount = FloatArgumentType.getFloat(context, "amount");
        DifficultySourceCapability.getOverworldCap().orElseGet(DifficultySourceCapability::new).addDifficulty(amount);
        return 1;
    }

    private static MutableComponent text(String key, Object... args) {
        return new TranslatableComponent("command.scalinghealth.difficulty." + key, args);
    }
}
