package net.silentchaos512.scalinghealth.client.gui.health;

public enum HealthTextStyle {
    DISABLED(1f) {
        @Override
        public String textFor(float current, float max) {
            return "";
        }
    },
    ROWS(0.65f) {
        @Override
        public String textFor(float current, float max) {
            return (int) Math.ceil(current / 20) + "x";
        }
    },
    HEALTH_AND_MAX(0.5f) {
        @Override
        public String textFor(float current, float max) {
            if (max == 0) return HEALTH_ONLY.textFor(current, max);
            return Math.round(current) + "/" + Math.round(max);
        }
    },
    HEALTH_ONLY(0.5f) {
        @Override
        public String textFor(float current, float max) {
            return String.valueOf(Math.round(current));
        }
    };

    private final float scale;

    HealthTextStyle(float scale) {
        this.scale = scale;
    }

    public abstract String textFor(float current, float max);

    public float getScale() {
        return scale;
    }
}
