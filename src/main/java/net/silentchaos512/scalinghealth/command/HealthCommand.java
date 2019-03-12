package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import net.minecraft.util.text.TextFormatting;
import net.silentchaos512.scalinghealth.capability.CapabilityPlayerData;
import net.silentchaos512.scalinghealth.utils.Players;

public final class HealthCommand {
    private HealthCommand() {}

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("sh_health").requires(source ->
                source.hasPermissionLevel(2));

        // get
        builder
                .then(Commands.literal("get")
                        .then(Commands.argument("targets", EntityArgument.multiplePlayers())
                                .executes(
                                        // Run for all targets
                                        HealthCommand::runGetHealth
                                )
                        )
                        .executes(context -> {
                            // No target, use sender
                            return getHealthSingle(context, context.getSource().asPlayer());
                        })
                );
        // set
        builder
                .then(Commands.literal("set")
                        .then(Commands.argument("targets", EntityArgument.multiplePlayers())
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
                        .then(Commands.argument("targets", EntityArgument.multiplePlayers())
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
        for (EntityPlayerMP player : EntityArgument.getPlayers(context, "targets")) {
            getHealthSingle(context, player);
        }
        return 1;
    }

    private static int getHealthSingle(CommandContext<CommandSource> context, EntityPlayer player) {
        player.getCapability(CapabilityPlayerData.INSTANCE).ifPresent(data -> {
            context.getSource().sendFeedback(ModCommands.playerNameText(player), true);
            // Actual health
            ITextComponent actualValues = ModCommands.valueText(player.getHealth(), player.getMaxHealth());
            ITextComponent actualText = text("actual", actualValues)
                    .applyTextStyle(TextFormatting.YELLOW);
            context.getSource().sendFeedback(actualText, true);
            // Heart crystals and health modifier
            int extraHearts = data.getExtraHearts();
            String extraHealth = (extraHearts >= 0 ? "+" : "") + (2 * extraHearts);
            ITextComponent heartsValues = text("heartCrystals.values", extraHearts, extraHealth)
                    .applyTextStyle(TextFormatting.WHITE);
            ITextComponent heartsText = text("heartCrystals", heartsValues)
                    .applyTextStyle(TextFormatting.YELLOW);
            context.getSource().sendFeedback(heartsText, true);
        });
        return 1;
    }

    private static int runSetHealth(CommandContext<CommandSource> context) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        for (EntityPlayerMP player : EntityArgument.getPlayers(context, "targets")) {
            player.getCapability(CapabilityPlayerData.INSTANCE).ifPresent(data -> {
                int intendedExtraHearts = (amount - Players.startingHealth(player)) / 2;
                data.setExtraHearts(player, intendedExtraHearts);
            });
        }
        return 1;
    }

    private static int runAddHealth(CommandContext<CommandSource> context) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        for (EntityPlayerMP player : EntityArgument.getPlayers(context, "targets")) {
            player.getCapability(CapabilityPlayerData.INSTANCE).ifPresent(data -> {
                data.addHearts(player, amount);
            });
        }
        return 1;
    }

    private static ITextComponent text(String key, Object... args) {
        return new TextComponentTranslation("command.scalinghealth.health." + key, args);
    }
}
