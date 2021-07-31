package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.silentchaos512.scalinghealth.capability.IPlayerData;
import net.silentchaos512.scalinghealth.utils.config.SHItems;
import net.silentchaos512.scalinghealth.utils.config.SHPlayers;

public final class HealthCommand {
    private HealthCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("sh_health").requires(source ->
                source.hasPermission(2));

        // get
        builder
                .then(Commands.literal("get")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(
                                        // Run for all targets
                                        HealthCommand::runGetHealth
                                )
                        )
                        .executes(context -> {
                            // No target, use sender
                            return getHealthSingle(context, context.getSource().getPlayerOrException());
                        })
                );
        // set
        builder
                .then(Commands.literal("set")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(
                                                HealthCommand::runSetHealth
                                        )
                                )
                        )
                );
        // add
        builder
                .then(Commands.literal("add")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(
                                                HealthCommand::runAddHealth
                                        )
                                )
                        )
                );

        dispatcher.register(builder);
    }

    private static int runGetHealth(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
            getHealthSingle(context, player);
        }
        return 1;
    }

    private static int getHealthSingle(CommandContext<CommandSourceStack> context, Player player) {
        IPlayerData data = SHPlayers.getPlayerData(player);

        context.getSource().sendSuccess(ModCommands.playerNameText(player), true);
        // Actual health
        MutableComponent actualValues = ModCommands.valueText(player.getHealth(), player.getMaxHealth());
        MutableComponent actualText = text("actual", actualValues)
                .withStyle(ChatFormatting.YELLOW);
        context.getSource().sendSuccess(actualText, true);
        // Heart crystals and health modifier
        int extraHearts = data.getHeartCrystals();
        String extraHealth = (extraHearts >= 0 ? "+" : "") + (2 * extraHearts);
        MutableComponent heartsValues = text("heartCrystals.values",extraHearts / SHItems.heartCrystalIncreaseAmount(), extraHealth)
                .withStyle(ChatFormatting.WHITE);
        MutableComponent heartsText = text("heartCrystals", heartsValues)
                .withStyle(ChatFormatting.YELLOW);
        context.getSource().sendSuccess(heartsText, true);
        return 1;
    }

    private static int runSetHealth(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
            IPlayerData data = SHPlayers.getPlayerData(player);
            int intendedExtraHearts = (amount - SHPlayers.startingHealth()) / 2;
            data.setHeartCrystals(player, intendedExtraHearts);
        }
        return 1;
    }

    private static int runAddHealth(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
            SHPlayers.getPlayerData(player).addHeartCrystals(player, amount);
        }
        return 1;
    }

    private static MutableComponent text(String key, Object... args) {
        return new TranslatableComponent("command.scalinghealth.health." + key, args);
    }
}
