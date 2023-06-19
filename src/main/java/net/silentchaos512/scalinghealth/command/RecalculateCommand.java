package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.silentchaos512.scalinghealth.utils.MobDifficultyHandler;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

import java.util.concurrent.atomic.AtomicInteger;

public final class RecalculateCommand {
    private RecalculateCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(Commands.literal("sh_recalculate").requires(source ->
                source.hasPermission(2))
                .then(Commands.literal("all")
                        .executes(context -> {
                            context.getSource().sendSuccess(() -> Component.translatable("command.scalinghealth.recalculate.start"), true);
                            int processed = recalculateAllEntities(context);
                            context.getSource().sendSuccess(() -> Component.translatable("command.scalinghealth.recalculate.finish", processed), true);
                            return 1;
                        })
                ));
    }

    private static int recalculateAllEntities(CommandContext<CommandSourceStack> context) {
        AtomicInteger processed = new AtomicInteger(0);
        context.getSource().getLevel().getEntities().getAll().forEach(entity -> {
            if (entity instanceof Mob mob) {
                MobDifficultyHandler.process(mob, SHDifficulty.affected(entity));
                processed.incrementAndGet();
            }
        });
        return processed.get();
    }
}
