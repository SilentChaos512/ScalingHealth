package net.silentchaos512.scalinghealth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntitySummonArgument;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.silentchaos512.scalinghealth.capability.IDifficultyAffected;
import net.silentchaos512.scalinghealth.utils.MobDifficultyHandler;
import net.silentchaos512.scalinghealth.utils.SHDifficulty;

public final class SummonCommand {
    private static final SimpleCommandExceptionType SUMMON_FAILED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.summon.failed"));

    private SummonCommand() {}

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("sh_summon").requires(source ->
                source.hasPermissionLevel(2));

        // blight summoning? setting difficulty?
        builder.then(Commands.argument("entity", EntitySummonArgument.entitySummon())
                .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                .executes(source ->
                        summonEntity(
                                source.getSource(),
                                EntitySummonArgument.getEntityId(source, "entity"),
                                -1,
                                false,
                                source.getSource().getPos(),
                                new CompoundNBT(),
                                true
                        )
                )
                .then(Commands.argument("difficulty", IntegerArgumentType.integer())
                        .executes(source ->
                                summonEntity(
                                        source.getSource(),
                                        EntitySummonArgument.getEntityId(source, "entity"),
                                        IntegerArgumentType.getInteger(source, "difficulty"),
                                        false,
                                        source.getSource().getPos(),
                                        new CompoundNBT(),
                                        true
                                )
                        )
                        .then(Commands.argument("forceBlight", BoolArgumentType.bool())
                                .executes(source ->
                                        summonEntity(
                                                source.getSource(),
                                                EntitySummonArgument.getEntityId(source, "entity"),
                                                IntegerArgumentType.getInteger(source, "difficulty"),
                                                BoolArgumentType.getBool(source, "forceBlight"),
                                                source.getSource().getPos(),
                                                new CompoundNBT(),
                                                true
                                        )
                                )
                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                        .executes(source ->
                                                summonEntity(
                                                        source.getSource(),
                                                        EntitySummonArgument.getEntityId(source, "entity"),
                                                        IntegerArgumentType.getInteger(source, "difficulty"),
                                                        BoolArgumentType.getBool(source, "forceBlight"),
                                                        Vec3Argument.getVec3(source, "pos"),
                                                        new CompoundNBT(),
                                                        true
                                                )
                                        ).then(Commands.argument("nbt", NBTCompoundTagArgument.nbt())
                                                .executes(source ->
                                                        summonEntity(
                                                                source.getSource(),
                                                                EntitySummonArgument.getEntityId(source, "entity"),
                                                                IntegerArgumentType.getInteger(source, "difficulty"),
                                                                BoolArgumentType.getBool(source, "forceBlight"),
                                                                Vec3Argument.getVec3(source, "pos"),
                                                                NBTCompoundTagArgument.getNbt(source, "nbt"),
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
    private static int summonEntity(CommandSource source, ResourceLocation id, int difficulty, boolean forceBlight, Vec3d pos, CompoundNBT tags, boolean randomizeProperties) throws CommandSyntaxException {
        CompoundNBT nbt = tags.copy();
        nbt.putString("id", id.toString());
        if (EntityType.getKey(EntityType.LIGHTNING_BOLT).equals(id)) {
            LightningBoltEntity lightningBolt = new LightningBoltEntity(source.func_197023_e(), pos.x, pos.y, pos.z, false);
            source.func_197023_e().addLightningBolt(lightningBolt);
            source.sendFeedback(new TranslationTextComponent("commands.summon.success", lightningBolt.getDisplayName()), true);
            return 1;
        } else {
            ServerWorld world = source.func_197023_e();
            Entity entity = EntityType.func_220335_a(nbt, world, e -> {
                e.setLocationAndAngles(pos.x, pos.y, pos.z, e.rotationYaw, e.rotationPitch);
                //noinspection ReturnOfNull
                return !world.summonEntity(e) ? null : e;
            });
            if (entity == null) {
                throw SUMMON_FAILED.create();
            } else {
                if (randomizeProperties && entity instanceof MobEntity) {
                    MobEntity mob = (MobEntity) entity;
                    mob.onInitialSpawn(world, world.getDifficultyForLocation(new BlockPos(entity)), SpawnReason.COMMAND, null, null);

                    if (difficulty > 0) {
                        IDifficultyAffected affected = SHDifficulty.affected(entity);
                        boolean blight = forceBlight || MobDifficultyHandler.shouldBecomeBlight(mob, difficulty);
                        affected.forceDifficulty(difficulty);
                        MobDifficultyHandler.setEntityProperties(mob, affected, blight);
                        affected.setProcessed(true);
                    }
                }
                source.sendFeedback(new TranslationTextComponent("commands.summon.success", entity.getDisplayName()), true);
                return 1;
            }
        }
    }
}
