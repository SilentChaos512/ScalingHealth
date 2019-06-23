package net.silentchaos512.scalinghealth.client.gui.health;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.MathHelper;
import net.silentchaos512.lib.event.ClientTicks;
import net.silentchaos512.scalinghealth.config.Config;

import java.util.Random;

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
    HeartIconStyle heartStyle = Config.CLIENT.heartIconStyle.get();
    AbsorptionIconStyle absorptionStyle = Config.CLIENT.absorptionIconStyle.get();
    // Other
    private final Random random = new Random();
    int scaledWindowWidth;
    int scaledWindowHeight;

    void update() {
        Minecraft mc = Minecraft.getInstance();
        //noinspection ConstantConditions -- can be null
        if (mc == null) return;
        PlayerEntity player = mc.player;
        if (player == null) return;

        int updateCounter = ClientTicks.ticksInGame();
        random.setSeed(updateCounter * 312871);
        scaledWindowWidth = mc.mainWindow.getScaledWidth();
        scaledWindowHeight = mc.mainWindow.getScaledHeight();

        health = player.getHealth();
        previousHealthInt = healthInt;
        healthInt = MathHelper.ceil(this.health);
        maxHealth = player.getMaxHealth();

        absorption = player.getAbsorptionAmount();
        absorptionInt = MathHelper.ceil(this.absorption);

        rowsUsedInHud = absorptionInt > 0 ? 2 : 1;
        rowHeight = rowsUsedInHud + 9; // wut?
        recentlyHurtHighlight = player.hurtResistantTime / 3 % 2 == 1;
        hardcoreMode = player.world.getWorldInfo().isHardcore();
        for (int i = 0; i < lowHealthBob.length; ++i) lowHealthBob[i] = random.nextInt(2);
        regenTimer = player.isPotionActive(Effects.REGENERATION) ? updateCounter % 20 : -1;

        heartStyle = Config.CLIENT.heartIconStyle.get();
        absorptionStyle = Config.CLIENT.absorptionIconStyle.get();
    }

    private boolean hasLowHealth() {
        return health <= maxHealth / 5;
    }

    int getCustomHeartRowCount(int healthAmount) {
        return heartStyle == HeartIconStyle.REPLACE_ALL
                ? MathHelper.ceil(healthAmount / 20f)
                : healthAmount / 20;
    }

    int getActualRow(int renderRowIndex) {
        return renderRowIndex + (heartStyle == HeartIconStyle.REPLACE_ALL ? 0 : 1);
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
            y += lowHealthBob[MathHelper.clamp(xIndex, 0, lowHealthBob.length - 1)];
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
}
