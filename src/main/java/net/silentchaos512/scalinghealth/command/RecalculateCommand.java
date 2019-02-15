package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.text.TextComponentTranslation;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultyAffected;
import net.silentchaos512.scalinghealth.utils.MobDifficultyHandler;

import java.util.concurrent.atomic.AtomicInteger;

public final class RecalculateCommand {
    private RecalculateCommand() {}

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("sh_recalculate").requires(source ->
                source.hasPermissionLevel(2)).then(
                Commands.literal("all").executes(context -> {
                    context.getSource().sendFeedback(new TextComponentTranslation("command.scalinghealth.recalculate.start"), true);
                    int processed = recalculateAllEntities(context);
                    context.getSource().sendFeedback(new TextComponentTranslation("command.scalinghealth.recalculate.finish", processed), true);
                    return 1;
                })
        ));
    }

    private static int recalculateAllEntities(CommandContext<CommandSource> context) {
        AtomicInteger processed = new AtomicInteger(0);
        context.getSource().getWorld().getEntities(EntityLivingBase.class, e -> true).forEach(entity -> {
            entity.getCapability(CapabilityDifficultyAffected.INSTANCE).ifPresent(affected -> {
                MobDifficultyHandler.process(entity, affected);
                processed.incrementAndGet();
            });
        });
        return processed.get();
    }
}
