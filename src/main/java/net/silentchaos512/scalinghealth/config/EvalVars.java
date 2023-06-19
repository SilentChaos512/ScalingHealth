package net.silentchaos512.scalinghealth.config;

import com.udojava.evalex.Expression;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

import javax.annotation.Nullable;
import java.util.function.Function;

public enum EvalVars {
    HEALTH("health", ctx -> ctx.player != null ? ctx.player.getHealth() : 20),
    MAX_HEALTH("maxHealth", ctx -> ctx.player != null ? ctx.player.getMaxHealth() : 20),
    FOOD("food", ctx -> ctx.player != null ? ctx.player.getFoodData().getFoodLevel() : 0),
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

    public static double apply(Player player, Expression expression) {
        return apply(player.level(), player.blockPosition(), player, expression);
    }

    public static double apply(Level world, BlockPos pos, @Nullable Player player, Expression expression) {
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
        private final Level world;
        private final BlockPos pos;
        private final Player player;

        private Context(Level world, BlockPos pos, @Nullable Player player) {
            this.world = world;
            this.pos = pos;
            this.player = player;
        }
    }
}
