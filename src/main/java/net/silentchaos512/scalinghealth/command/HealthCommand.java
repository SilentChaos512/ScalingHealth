package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.silentchaos512.scalinghealth.capability.IPlayerData;
import net.silentchaos512.scalinghealth.utils.config.SHItems;
import net.silentchaos512.scalinghealth.utils.config.SHPlayers;

public final class HealthCommand {
    private HealthCommand() {}

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("sh_health").requires(source ->
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

    private static int runGetHealth(CommandContext<CommandSource> context) throws CommandSyntaxException {
        for (ServerPlayerEntity player : EntityArgument.getPlayers(context, "targets")) {
            getHealthSingle(context, player);
        }
        return 1;
    }

    private static int getHealthSingle(CommandContext<CommandSource> context, PlayerEntity player) {
        IPlayerData data = SHPlayers.getPlayerData(player);

        context.getSource().sendSuccess(ModCommands.playerNameText(player), true);
        // Actual health
        IFormattableTextComponent actualValues = ModCommands.valueText(player.getHealth(), player.getMaxHealth());
        IFormattableTextComponent actualText = text("actual", actualValues)
                .withStyle(TextFormatting.YELLOW);
        context.getSource().sendSuccess(actualText, true);
        // Heart crystals and health modifier
        int extraHearts = data.getHeartCrystals();
        String extraHealth = (extraHearts >= 0 ? "+" : "") + (2 * extraHearts);
        IFormattableTextComponent heartsValues = text("heartCrystals.values",extraHearts / SHItems.heartCrystalIncreaseAmount(), extraHealth)
                .withStyle(TextFormatting.WHITE);
        IFormattableTextComponent heartsText = text("heartCrystals", heartsValues)
                .withStyle(TextFormatting.YELLOW);
        context.getSource().sendSuccess(heartsText, true);
        return 1;
    }

    private static int runSetHealth(CommandContext<CommandSource> context) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        for (ServerPlayerEntity player : EntityArgument.getPlayers(context, "targets")) {
            IPlayerData data = SHPlayers.getPlayerData(player);
            int intendedExtraHearts = (amount - SHPlayers.startingHealth()) / 2;
            data.setHeartCrystals(player, intendedExtraHearts);
        }
        return 1;
    }

    private static int runAddHealth(CommandContext<CommandSource> context) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        for (ServerPlayerEntity player : EntityArgument.getPlayers(context, "targets")) {
            SHPlayers.getPlayerData(player).addHeartCrystals(player, amount);
        }
        return 1;
    }

    private static IFormattableTextComponent text(String key, Object... args) {
        return new TranslationTextComponent("command.scalinghealth.health." + key, args);
    }
}
