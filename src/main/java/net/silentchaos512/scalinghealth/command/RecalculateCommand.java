package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.silentchaos512.scalinghealth.utils.MobDifficultyHandler;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

import java.util.concurrent.atomic.AtomicInteger;

public final class RecalculateCommand {
    private RecalculateCommand() {}

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("sh_recalculate").requires(source ->
                source.hasPermission(2))
                .then(Commands.literal("all")
                        .executes(context -> {
                            context.getSource().sendSuccess(new TranslationTextComponent("command.scalinghealth.recalculate.start"), true);
                            int processed = recalculateAllEntities(context);
                            context.getSource().sendSuccess(new TranslationTextComponent("command.scalinghealth.recalculate.finish", processed), true);
                            return 1;
                        })
                ));
    }

    private static int recalculateAllEntities(CommandContext<CommandSource> context) {
        AtomicInteger processed = new AtomicInteger(0);
        context.getSource().getLevel().getEntities().filter(e -> e instanceof MobEntity).forEach(entity -> {
            MobDifficultyHandler.process((MobEntity) entity, SHDifficulty.affected(entity));
            processed.incrementAndGet();
        });
        return processed.get();
    }
}
