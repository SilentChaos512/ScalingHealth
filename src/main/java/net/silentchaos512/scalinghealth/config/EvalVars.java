package net.silentchaos512.scalinghealth.config;

import com.udojava.evalex.Expression;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

import javax.annotation.Nullable;
import java.util.function.Function;

public enum EvalVars {
    HEALTH("health", ctx -> ctx.player != null ? ctx.player.getHealth() : 20),
    MAX_HEALTH("maxHealth", ctx -> ctx.player != null ? ctx.player.getMaxHealth() : 20),
    FOOD("food", ctx -> ctx.player != null ? ctx.player.getFoodStats().getFoodLevel() : 0),
    PLAYER_DIFFICULTY("difficulty", ctx -> ctx.player == null ? 0 : SHDifficulty.source(ctx.player).getDifficulty()),
    MAX_DIFFICULTY("maxDifficulty", ctx -> SHDifficulty.maxValue()),
    AREA_DIFFICULTY("areaDifficulty", ctx -> SHDifficulty.areaDifficulty(ctx.world, ctx.pos, false)),
    AREA_PLAYER_COUNT("areaPlayerCount", ctx ->
        SHDifficulty.playersInRange(ctx.world, ctx.pos, SHDifficulty.groupSearchRadius()).count()
    );

    private final String name;
    private final Function<Context, ? extends Number> value;

    EvalVars(String name, Function<Context, ? extends Number> value) {
        this.name = name;
        this.value = value;
    }

    public String varName() {
        return name;
    }

    public static double apply(PlayerEntity player, Expression expression) {
        return apply(player.world, player.getPosition(), player, expression);
    }

    public static double apply(World world, BlockPos pos, @Nullable PlayerEntity player, Expression expression) {
        Context context = new Context(world, pos, player);
        for (EvalVars variable : values()) {
            expression.setVariable(variable.varName(), variable.value.apply(context).toString());
        }
        return expression.eval().doubleValue();
    }

    public static Expression dummyPopulate(Expression expression) {
        for (EvalVars var : values())
            expression.setVariable(var.varName(), "1");
        return expression;
    }

    private static final class Context {
        private final World world;
        private final BlockPos pos;
        private final PlayerEntity player;

        private Context(World world, BlockPos pos, @Nullable PlayerEntity player) {
            this.world = world;
            this.pos = pos;
            this.player = player;
        }
    }
}
