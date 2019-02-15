package net.silentchaos512.scalinghealth.config;

import com.udojava.evalex.Expression;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.utils.Difficulty;

import javax.annotation.Nullable;
import java.util.function.Function;

public enum EvalVars {
    HEALTH("health", ctx ->
            ctx.player != null ? ctx.player.getHealth() : 20
    ),
    MAX_HEALTH("maxHealth", ctx ->
            ctx.player != null ? ctx.player.getMaxHealth() : 20
    ),
    FOOD("food", ctx ->
            ctx.player != null ? ctx.player.getFoodStats().getFoodLevel() : 0
    ),
    PLAYER_DIFFICULTY("difficulty", ctx -> {
        if (ctx.player == null) return 0;
        return Difficulty.source(ctx.player).getDifficulty();
    }),
    MAX_DIFFICULTY("maxDifficulty", ctx ->
            Difficulty.maxValue(ctx.world)
    ),
    AREA_DIFFICULTY("areaDifficulty", ctx ->
            Difficulty.areaDifficulty(ctx.world, ctx.pos, false)
    ),
    AREA_PLAYER_COUNT("areaPlayerCount", ctx -> {
        long radiusSquared = Difficulty.searchRadiusSquared(ctx.world);
        return ctx.world.getPlayers(EntityPlayer.class, p ->
                radiusSquared == 0 || p.getDistanceSq(ctx.pos) <= radiusSquared)
                .size();
    });

    private final String name;
    private final Function<Context, ? extends Number> value;

    EvalVars(String name, Function<Context, ? extends Number> value) {
        this.name = name;
        this.value = value;
    }

    public String varName() {
        return name;
    }

    public static double apply(DimensionConfig config, World world, BlockPos pos, @Nullable EntityPlayer player, Expression expression) {
        Context context = new Context(config, world, pos, player);
        for (EvalVars variable : values()) {
            // TODO: Is toString right?
            expression.setVariable(variable.varName(), variable.value.apply(context).toString());
        }
        return expression.eval().doubleValue();
    }

    private static class Context {
        private final DimensionConfig config;
        private final World world;
        private final BlockPos pos;
        @Nullable
        private final EntityPlayer player;

        private Context(DimensionConfig config, World world, BlockPos pos, @Nullable EntityPlayer player) {
            this.config = config;
            this.world = world;
            this.pos = pos;
            this.player = player;
        }
    }
}
