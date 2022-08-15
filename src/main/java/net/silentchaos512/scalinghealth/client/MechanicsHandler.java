package net.silentchaos512.scalinghealth.client;

import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.resources.mechanics.SHMechanics;

public class MechanicsHandler {
    private static SHMechanics mechanics = SHMechanics.DEFAULT;

    public static void setClientMechanics(SHMechanics shMechanics) {
        mechanics = shMechanics;
        ScalingHealth.LOGGER.debug("Loaded SHMechanics on the client.");
    }

    public static SHMechanics getClientMechanics() {
        return mechanics;
    }
}
