package net.silentchaos512.scalinghealth.capability;

public interface IDifficultySource {
    /**
     * Get the difficulty of this source.
     *
     * @return The source's difficulty
     */
    float getDifficulty(); //TODO change to double when updating to 1.17.

    void setExempt(boolean exempt);

    /**
     * Set the difficulty of this source. Consider using {@link #addDifficulty(float)} instead.
     *
     * @param value The new difficulty value
     */
    void setDifficulty(float value);

    /**
     * Add (or subtract if amount is negative) this amount to current difficulty.
     *
     * @param amount Amount to add. May be negative.
     */
    default void addDifficulty(float amount) {
        setDifficulty(getDifficulty() + amount);
    }
}
