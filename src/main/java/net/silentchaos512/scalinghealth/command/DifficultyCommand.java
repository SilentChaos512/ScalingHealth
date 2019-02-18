package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultySource;
import net.silentchaos512.scalinghealth.utils.Difficulty;

public final class DifficultyCommand {
    private DifficultyCommand() {}

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("sh_difficulty").requires(source ->
                source.hasPermissionLevel(2));

        // get
        builder.then(
                Commands.literal("get").then(
                        Commands.argument("targets", EntityArgument.multiplePlayers()).executes(
                                // Run for all targets
                                DifficultyCommand::runGetDifficulty
                        )
                ).then (
                        Commands.literal("world").executes(
                                DifficultyCommand::runGetWorldDifficulty
                        )
                ).executes(context -> {
                    // No target, use sender
                    return getDifficultySingle(context, context.getSource().asPlayer());
                })
        );
        // set
        builder.then(
                Commands.literal("set").then(
                        Commands.argument("targets", EntityArgument.multiplePlayers()).then(
                                Commands.argument("amount", FloatArgumentType.floatArg()).executes(
                                        DifficultyCommand::runSetDifficulty
                                )
                        )
                ).then(
                        Commands.literal("world").then(
                                Commands.argument("amount", FloatArgumentType.floatArg()).executes(
                                        DifficultyCommand::runSetWorldDifficulty
                                )
                        )
                )
        );
        // add
        builder.then(
                Commands.literal("add").then(
                        Commands.argument("targets", EntityArgument.multiplePlayers()).then(
                                Commands.argument("amount", FloatArgumentType.floatArg()).executes(
                                        DifficultyCommand::runAddDifficulty
                                )
                        )
                ).then(
                        Commands.literal("world").then(
                                Commands.argument("amount", FloatArgumentType.floatArg()).executes(
                                        DifficultyCommand::runAddWorldDifficulty
                                )
                        )
                )
        );

        dispatcher.register(builder);
    }

    private static int runGetDifficulty(CommandContext<CommandSource> context) throws CommandSyntaxException {
        for (EntityPlayerMP player : EntityArgument.getPlayers(context, "targets")) {
            getDifficultySingle(context, player);
        }
        return 1;
    }

    private static int getDifficultySingle(CommandContext<CommandSource> context, EntityPlayer player) {
        player.getCapability(CapabilityDifficultySource.INSTANCE).ifPresent(source -> {
            context.getSource().sendFeedback(ModCommands.playerNameText(player), true);
            double maxDifficulty = Difficulty.maxValue(player.world);
            // Player difficulty
            float difficulty = source.getDifficulty();
            ITextComponent playerValues = ModCommands.valueText(difficulty, maxDifficulty);
            ITextComponent playerText = text("player", playerValues)
                    .applyTextStyle(TextFormatting.YELLOW);
            context.getSource().sendFeedback(playerText, true);
            // Area difficulty
            double areaDifficulty = Difficulty.areaDifficulty(player.world, player.getPosition());
            ITextComponent areaValues = ModCommands.valueText(areaDifficulty, maxDifficulty);
            ITextComponent areaText = text("area", areaValues)
                    .applyTextStyle(TextFormatting.YELLOW);
            // Area mode
            ITextComponent modeText = new TextComponentString(" (")
                    .applyTextStyle(TextFormatting.GRAY)
                    .appendSibling(Difficulty.areaMode(player.world).getDisplayName())
                    .appendText(")");
            areaText.appendSibling(modeText);
            context.getSource().sendFeedback(areaText, true);
        });
        return 1;
    }

    private static int runGetWorldDifficulty(CommandContext<CommandSource> context) {
        WorldServer world = context.getSource().getWorld();
        world.getCapability(CapabilityDifficultySource.INSTANCE).ifPresent(source -> {
            ITextComponent textWorld = new TextComponentTranslation("command.scalinghealth.worldName", world.dimension.getType().getId())
                    .applyTextStyle(TextFormatting.AQUA);
            context.getSource().sendFeedback(textWorld, true);
            // Difficulty
            double difficulty = source.getDifficulty();
            double maxDifficulty = Difficulty.maxValue(world);
            ITextComponent textValues = ModCommands.valueText(difficulty, maxDifficulty);
            ITextComponent textDifficulty = text("world", textValues)
                    .applyTextStyle(TextFormatting.YELLOW);
            context.getSource().sendFeedback(textDifficulty, true);
        });
        return 1;
    }

    private static int runSetDifficulty(CommandContext<CommandSource> context) throws CommandSyntaxException {
        float amount = FloatArgumentType.getFloat(context, "amount");
        for (EntityPlayerMP player : EntityArgument.getPlayers(context, "targets")) {
            player.getCapability(CapabilityDifficultySource.INSTANCE).ifPresent(source -> {
                source.setDifficulty(amount);
            });
        }
        return 1;
    }

    private static int runSetWorldDifficulty(CommandContext<CommandSource> context) {
        float amount = FloatArgumentType.getFloat(context, "amount");
        WorldServer world = context.getSource().getWorld();
        world.getCapability(CapabilityDifficultySource.INSTANCE).ifPresent(source -> {
            source.setDifficulty(amount);
        });
        return 1;
    }

    private static int runAddDifficulty(CommandContext<CommandSource> context) throws CommandSyntaxException {
        float amount = FloatArgumentType.getFloat(context, "amount");
        for (EntityPlayerMP player : EntityArgument.getPlayers(context, "targets")) {
            player.getCapability(CapabilityDifficultySource.INSTANCE).ifPresent(source -> {
                source.addDifficulty(amount);
            });
        }
        return 1;
    }

    private static int runAddWorldDifficulty(CommandContext<CommandSource> context) {
        float amount = FloatArgumentType.getFloat(context, "amount");
        WorldServer world = context.getSource().getWorld();
        world.getCapability(CapabilityDifficultySource.INSTANCE).ifPresent(source -> {
            source.addDifficulty(amount);
        });
        return 1;
    }

    private static ITextComponent text(String key, Object... args) {
        return new TextComponentTranslation("command.scalinghealth.difficulty." + key, args);
    }
}
