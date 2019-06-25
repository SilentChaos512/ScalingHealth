package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.silentchaos512.scalinghealth.capability.DifficultyAffectedCapability;
import net.silentchaos512.scalinghealth.utils.MobDifficultyHandler;

import java.util.concurrent.atomic.AtomicInteger;

public final class RecalculateCommand {
    private RecalculateCommand() {}

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("sh_recalculate").requires(source ->
                source.hasPermissionLevel(2))
                .then(Commands.literal("all")
                        .executes(context -> {
                            context.getSource().sendFeedback(new TranslationTextComponent("command.scalinghealth.recalculate.start"), true);
                            int processed = recalculateAllEntities(context);
                            context.getSource().sendFeedback(new TranslationTextComponent("command.scalinghealth.recalculate.finish", processed), true);
                            return 1;
                        })
                ));
    }

    private static int recalculateAllEntities(CommandContext<CommandSource> context) {
        AtomicInteger processed = new AtomicInteger(0);
        context.getSource().getWorld().getEntities().filter(e -> e instanceof MobEntity).forEach(entity -> {
            entity.getCapability(DifficultyAffectedCapability.INSTANCE).ifPresent(affected -> {
                MobDifficultyHandler.process((MobEntity) entity, affected);
                processed.incrementAndGet();
            });
        });
        return processed.get();
    }
}
