package net.silentchaos512.scalinghealth.client.gui.health;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.silentchaos512.lib.event.ClientTicks;
import net.silentchaos512.scalinghealth.config.SHConfig;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Stores various fields used by HeartDisplayHandler. All necessary fields are updated once per
 * render tick. This reduces the complexity of HeartDisplayHandler, cuts down on duplicate code, and
 * may improve performance slightly.
 */
class HeartsInfo {
    float health;
    int healthInt;
    int previousHealthInt;
    float maxHealth = 20;
    float absorption;
    int absorptionInt;
    // Render values
    int rowsUsedInHud;
    int rowHeight;
    boolean recentlyHurtHighlight;
    boolean hardcoreMode;
    private final int[] lowHealthBob = new int[10];
    private int regenTimer;
    // Configs
    Supplier<HeartIconStyle> heartStyle = SHConfig.CLIENT.heartIconStyle;
    Supplier<AbsorptionIconStyle> absorptionStyle = SHConfig.CLIENT.absorptionIconStyle;
    // Other
    private final Random random = new Random();
    int scaledWindowWidth;
    int scaledWindowHeight;

    void update() {
        Minecraft mc = Minecraft.getInstance();
        //noinspection ConstantConditions -- can be null
        if (mc == null) return;
        Player player = mc.player;
        if (player == null) return;

        int updateCounter = ClientTicks.ticksInGame();
        random.setSeed(updateCounter * 312871);
        scaledWindowWidth = mc.getWindow().getGuiScaledWidth();
        scaledWindowHeight = mc.getWindow().getGuiScaledHeight();

        health = player.getHealth();
        previousHealthInt = healthInt;
        healthInt = Mth.ceil(this.health);
        maxHealth = player.getMaxHealth();

        absorption = player.getAbsorptionAmount();
        absorptionInt = Mth.ceil(this.absorption);

        rowsUsedInHud = absorptionInt > 0 ? 2 : 1;
        rowHeight = rowsUsedInHud + 9; // wut?
        recentlyHurtHighlight = player.invulnerableTime / 3 % 2 == 1;
        hardcoreMode = player.level.getLevelData().isHardcore();
        for (int i = 0; i < lowHealthBob.length; ++i) lowHealthBob[i] = random.nextInt(2);
        regenTimer = player.hasEffect(MobEffects.REGENERATION) ? updateCounter % 20 : -1;
    }

    private boolean hasLowHealth() {
        return health <= maxHealth / 5;
    }

    int getCustomHeartRowCount(int healthAmount) {
        return heartStyle.get() == HeartIconStyle.REPLACE_ALL
                ? Mth.ceil(healthAmount / 20f)
                : healthAmount / 20;
    }

    int getActualRow(int renderRowIndex) {
        return renderRowIndex + (heartStyle.get() == HeartIconStyle.REPLACE_ALL ? 0 : 1);
    }

    int getHeartsInRows(int actualRow) {
        return Math.min((healthInt - 20 * actualRow) / 2, 10);
    }

    int offsetHeartPosY(int xIndex, int initialY) {
        int y = initialY;
        if (xIndex == regenTimer) {
            y -= 2;
        }
        if (hasLowHealth()) {
            y += lowHealthBob[Mth.clamp(xIndex, 0, lowHealthBob.length - 1)];
        }
        return y;
    }

    int offsetAbsorptionPosY(int xIndex, int initialY) {
        int y = initialY - 10;
        if (xIndex == regenTimer - 10) {
            y -= 2;
        }
        return y;
    }

    // Tank style

    int getHeartTanks() {
        return healthInt / 20;
    }

    int getMaxHeartTanks() {
        return (int) maxHealth / 20;
    }

    int getHeartTankRowCount() {
        return Mth.ceil(getHeartTanks() / 20f);
    }

    int getMaxHeartTankRowCount() {
        return Mth.ceil(getMaxHeartTanks() / 20f);
    }

    int getFilledHeartTanksInRow(int row) {
        return Math.min(getHeartTanks() - 20 * row, 20);
    }

    int getAllHeartTanksInRow(int row) {
        return Math.min(getMaxHeartTanks() - 20 * row, 20);
    }
}
