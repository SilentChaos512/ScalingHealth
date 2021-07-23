package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.silentchaos512.scalinghealth.capability.IPlayerData;
import net.silentchaos512.scalinghealth.utils.config.SHItems;
import net.silentchaos512.scalinghealth.utils.config.SHPlayers;

public final class PowerCommand {
    private PowerCommand() {}

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("sh_power").requires(source ->
                source.hasPermission(2));

        // get
        builder
                .then(Commands.literal("get")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(
                                        // Run for all targets
                                        PowerCommand::runGet
                                )
                        )
                        .executes(context -> {
                            // No target, use sender
                            return runGetSingle(context, context.getSource().getPlayerOrException());
                        })
                );
        // set
        builder
                .then(Commands.literal("set")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(
                                                PowerCommand::runSet
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
                                                PowerCommand::runAdd
                                        )
                                )
                        )
                );

        dispatcher.register(builder);
    }

    private static int runGet(CommandContext<CommandSource> context) throws CommandSyntaxException {
        for (ServerPlayerEntity player : EntityArgument.getPlayers(context, "targets")) {
            runGetSingle(context, player);
        }
        return 1;
    }

    private static int runGetSingle(CommandContext<CommandSource> context, PlayerEntity player) {
        IPlayerData data = SHPlayers.getPlayerData(player);
        context.getSource().sendSuccess(ModCommands.playerNameText(player), true);
        // Actual power
        ModifiableAttributeInstance attr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        ITextComponent actualText = text("actual", String.format("%.1f", attr.getValue()))
                .copy().withStyle(TextFormatting.YELLOW);
        context.getSource().sendSuccess(actualText, true);
        // Crystals and modifier
        int crystals = data.getPowerCrystals();
        String extraPower = (crystals >= 0 ? "+" : "") + (data.getAttackDamageModifier());
        ITextComponent crystalsValues = text("powerCrystals.values", crystals, extraPower)
                .copy().withStyle(TextFormatting.WHITE);
        ITextComponent crystalsText = text("powerCrystals", crystalsValues)
                .copy().withStyle(TextFormatting.YELLOW);
        context.getSource().sendSuccess(crystalsText, true);
        return 1;
    }

    private static int runSet(CommandContext<CommandSource> context) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        for (ServerPlayerEntity player : EntityArgument.getPlayers(context, "targets")) {
            IPlayerData data = SHPlayers.getPlayerData(player);
            int intendedCrystalCount = (int) ((amount - 1) / SHItems.powerCrystalIncreaseAmount()); //-1 because 1 is the default damage.
            data.setPowerCrystalCount(player, intendedCrystalCount);
        }
        return 1;
    }

    private static int runAdd(CommandContext<CommandSource> context) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        for (ServerPlayerEntity player : EntityArgument.getPlayers(context, "targets")) {
            IPlayerData data = SHPlayers.getPlayerData(player);
            data.addPowerCrystals(player, amount);
        }
        return 1;
    }

    private static ITextComponent text(String key, Object... args) {
        return new TranslationTextComponent("command.scalinghealth.power." + key, args);
    }
}
