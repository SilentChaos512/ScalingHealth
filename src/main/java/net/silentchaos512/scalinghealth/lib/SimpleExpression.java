package net.silentchaos512.scalinghealth.lib;

import java.util.Optional;

public final class SimpleExpression {
    private enum Operation {
        ADD,
        SUB,
        MUL,
        DIV
    }

    private final Operation operation;
    private final float value;

    private SimpleExpression(String str) {
        char c = str.charAt(0);
        if (c == '*' || c == 'x') {
            this.operation = Operation.MUL;
        } else if (c == '/') {
            this.operation = Operation.DIV;
        } else if (c == '-') {
            this.operation = Operation.SUB;
        } else {
            this.operation = Operation.ADD;
        }

        this.value = Float.parseFloat(str.substring(1));
    }

    public static Optional<SimpleExpression> from(String str) {
        try {
            return Optional.of(new SimpleExpression(str));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public double apply(double value) {
        switch (this.operation) {
            case ADD:
                return value + this.value;
            case SUB:
                return value - this.value;
            case MUL:
                return value * this.value;
            case DIV:
                return value / this.value;
            default:
                return value;
        }
    }
}
