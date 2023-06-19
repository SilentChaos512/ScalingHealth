package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.silentchaos512.scalinghealth.capability.IDifficultyAffected;
import net.silentchaos512.scalinghealth.utils.MobDifficultyHandler;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

public final class SummonCommand {
    private static final SimpleCommandExceptionType SUMMON_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.summon.failed"));

    private SummonCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("sh_summon").requires(source ->
                source.hasPermission(2));

        // blight summoning? setting difficulty?
        builder.then(Commands.argument("entity", ResourceArgument.resource(context, Registries.ENTITY_TYPE))
                .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                .executes(source ->
                        summonEntity(
                                source.getSource(),
                                ResourceArgument.getEntityType(source, "entity").key().location(),
                                -1,
                                false,
                                source.getSource().getPosition(),
                                new CompoundTag(),
                                true
                        )
                )
                .then(Commands.argument("difficulty", IntegerArgumentType.integer())
                        .executes(source ->
                                summonEntity(
                                        source.getSource(),
                                        ResourceArgument.getEntityType(source, "entity").key().location(),
                                        IntegerArgumentType.getInteger(source, "difficulty"),
                                        false,
                                        source.getSource().getPosition(),
                                        new CompoundTag(),
                                        true
                                )
                        )
                        .then(Commands.argument("forceBlight", BoolArgumentType.bool())
                                .executes(source ->
                                        summonEntity(
                                                source.getSource(),
                                                ResourceArgument.getEntityType(source, "entity").key().location(),
                                                IntegerArgumentType.getInteger(source, "difficulty"),
                                                BoolArgumentType.getBool(source, "forceBlight"),
                                                source.getSource().getPosition(),
                                                new CompoundTag(),
                                                true
                                        )
                                )
                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                        .executes(source ->
                                                summonEntity(
                                                        source.getSource(),
                                                        ResourceArgument.getEntityType(source, "entity").key().location(),
                                                        IntegerArgumentType.getInteger(source, "difficulty"),
                                                        BoolArgumentType.getBool(source, "forceBlight"),
                                                        Vec3Argument.getVec3(source, "pos"),
                                                        new CompoundTag(),
                                                        true
                                                )
                                        ).then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
                                                .executes(source ->
                                                        summonEntity(
                                                                source.getSource(),
                                                                ResourceArgument.getEntityType(source, "entity").key().location(),                                                                IntegerArgumentType.getInteger(source, "difficulty"),
                                                                BoolArgumentType.getBool(source, "forceBlight"),
                                                                Vec3Argument.getVec3(source, "pos"),
                                                                CompoundTagArgument.getCompoundTag(source, "nbt"),
                                                                false
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );

        dispatcher.register(builder);
    }

    // Mostly a copy of vanilla summon command
    private static int summonEntity(CommandSourceStack source, ResourceLocation id, int difficulty, boolean forceBlight, Vec3 pos, CompoundTag tags, boolean randomizeProperties) throws CommandSyntaxException {
        CompoundTag nbt = tags.copy();
        nbt.putString("id", id.toString());
        ServerLevel world = source.getLevel();
        Entity entity = EntityType.loadEntityRecursive(nbt, world, e -> {
            e.moveTo(pos.x, pos.y, pos.z, e.getYRot(), e.getXRot());
            //noinspection ReturnOfNull
            return !world.addWithUUID(e) ? null : e;
        });
        if (entity == null) {
            throw SUMMON_FAILED.create();
        } else {
            if (randomizeProperties && entity instanceof Mob) {
                Mob mob = (Mob) entity;
                mob.finalizeSpawn(world, world.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.COMMAND, null, null);

                if (difficulty > 0) {
                    IDifficultyAffected affected = SHDifficulty.affected(entity);
                    boolean blight = forceBlight || MobDifficultyHandler.shouldBecomeBlight(mob, difficulty);
                    affected.forceDifficulty(difficulty);
                    MobDifficultyHandler.setEntityProperties(mob, affected, blight);
                    affected.setProcessed(true);
                }
            }
            source.sendSuccess(() -> Component.translatable("commands.summon.success", entity.getDisplayName()), true);
            return 1;
        }
    }
}
