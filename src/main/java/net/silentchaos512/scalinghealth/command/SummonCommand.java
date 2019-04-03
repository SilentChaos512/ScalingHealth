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
import net.minecraft.command.arguments.NBTArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.silentchaos512.scalinghealth.capability.CapabilityDifficultyAffected;
import net.silentchaos512.scalinghealth.utils.MobDifficultyHandler;

public final class SummonCommand {
    private static final SimpleCommandExceptionType field_198741_a = new SimpleCommandExceptionType(new TextComponentTranslation("commands.summon.failed"));

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
                                new NBTTagCompound(),
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
                                        new NBTTagCompound(),
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
                                                new NBTTagCompound(),
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
                                                        new NBTTagCompound(),
                                                        true
                                                )
                                        ).then(Commands.argument("nbt", NBTArgument.nbt())
                                                .executes(source ->
                                                        summonEntity(
                                                                source.getSource(),
                                                                EntitySummonArgument.getEntityId(source, "entity"),
                                                                IntegerArgumentType.getInteger(source, "difficulty"),
                                                                BoolArgumentType.getBool(source, "forceBlight"),
                                                                Vec3Argument.getVec3(source, "pos"),
                                                                NBTArgument.getNBT(source, "nbt"),
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
    private static int summonEntity(CommandSource source, ResourceLocation id, int difficulty, boolean forceBlight, Vec3d pos, NBTTagCompound tags, boolean p_198737_4_) throws CommandSyntaxException {
        NBTTagCompound nbttagcompound = tags.copy();
        nbttagcompound.putString("id", id.toString());
        if (EntityType.getId(EntityType.LIGHTNING_BOLT).equals(id)) {
            Entity entity1 = new EntityLightningBolt(source.getWorld(), pos.x, pos.y, pos.z, false);
            source.getWorld().addWeatherEffect(entity1);
            source.sendFeedback(new TextComponentTranslation("commands.summon.success", entity1.getDisplayName()), true);
            return 1;
        } else {
            Entity entity = AnvilChunkLoader.readWorldEntityPos(nbttagcompound, source.getWorld(), pos.x, pos.y, pos.z, true);
            if (entity == null) {
                throw field_198741_a.create();
            } else {
                entity.setLocationAndAngles(pos.x, pos.y, pos.z, entity.rotationYaw, entity.rotationPitch);
                if (p_198737_4_ && entity instanceof EntityLiving) {
                    EntityLiving entityLiving = (EntityLiving) entity;
                    entityLiving.onInitialSpawn(source.getWorld().getDifficultyForLocation(new BlockPos(entity)), null, null);

                    if (difficulty > 0) {
                        entity.getCapability(CapabilityDifficultyAffected.INSTANCE).ifPresent(affected -> {
                            boolean blight = forceBlight || MobDifficultyHandler.shouldBecomeBlight(entityLiving, difficulty);
                            MobDifficultyHandler.setEntityProperties(entityLiving, affected, difficulty, blight);
                            affected.setProcessed(true);
                        });
                    }
                }

                source.sendFeedback(new TextComponentTranslation("commands.summon.success", entity.getDisplayName()), true);
                return 1;
            }
        }
    }
}
