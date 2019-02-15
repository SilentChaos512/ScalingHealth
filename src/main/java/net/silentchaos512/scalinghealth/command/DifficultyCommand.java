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
import net.minecraft.util.text.TextComponentTranslation;
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
                                DifficultyCommand::runGetHealth
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
                )
        );

        dispatcher.register(builder);
    }

    private static int runGetHealth(CommandContext<CommandSource> context) throws CommandSyntaxException {
        for (EntityPlayerMP player : EntityArgument.getPlayers(context, "targets")) {
            getDifficultySingle(context, player);
        }
        return 1;
    }

    private static int getDifficultySingle(CommandContext<CommandSource> context, EntityPlayer player) {
        player.getCapability(CapabilityDifficultySource.INSTANCE).ifPresent(source -> {
            context.getSource().sendFeedback(ModCommands.playerNameText(player), true);
            float difficulty = source.getDifficulty();
            double maxDifficulty = Difficulty.maxValue(player.world);
            context.getSource().sendFeedback(text("player", difficulty, maxDifficulty), true);
            double areaDifficulty = Difficulty.forPos(player.world, player.getPosition());
            context.getSource().sendFeedback(text("area", areaDifficulty, maxDifficulty), true);
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

    private static int runAddDifficulty(CommandContext<CommandSource> context) throws CommandSyntaxException {
        float amount = FloatArgumentType.getFloat(context, "amount");
        for (EntityPlayerMP player : EntityArgument.getPlayers(context, "targets")) {
            player.getCapability(CapabilityDifficultySource.INSTANCE).ifPresent(source -> {
                source.addDifficulty(amount);
            });
        }
        return 1;
    }

    private static ITextComponent text(String key, Object... args) {
        return new TextComponentTranslation("command.scalinghealth.difficulty." + key, args);
    }
}
